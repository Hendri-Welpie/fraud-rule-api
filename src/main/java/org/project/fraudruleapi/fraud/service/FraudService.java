package org.project.fraudruleapi.fraud.service;

import io.micrometer.core.instrument.Counter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.evaluator.FraudEvaluator;
import org.project.fraudruleapi.fraud.mapper.TransactionMapper;
import org.project.fraudruleapi.fraud.model.EvaluationResult;
import org.project.fraudruleapi.fraud.model.FraudDetectionResponse;
import org.project.fraudruleapi.fraud.model.RuleDefinition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.fraud.repository.FraudRepository;
import org.project.fraudruleapi.fraud.repository.TransactionRepository;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.project.fraudruleapi.shared.config.ApplicationConfiguration;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudService {

    private final RuleCache ruleCache;
    private final FraudEvaluator fraudEvaluator;
    private final FraudRepository fraudRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationConfiguration config;
    private final VelocityCheckService velocityCheckService;
    private final Counter fraudDetectionCounter;
    private final Counter transactionProcessedCounter;

    @Transactional
    public Mono<FraudDetectionResponse> validate(@Valid TransactionDto transaction) {
        Instant startTime = Instant.now();

        return saveTransaction(transaction)
                .then(performFraudChecks(transaction))
                .flatMap(results -> {
                    boolean isFraud = !results.isEmpty();
                    int riskScore = fraudEvaluator.calculateRiskScore(results);
                    String severity = fraudEvaluator.determineSeverity(riskScore);

                    FraudDetectionResponse response = FraudDetectionResponse.builder()
                            .transactionId(transaction.transactionId())
                            .isFraud(isFraud)
                            .riskScore(riskScore)
                            .severity(severity)
                            .matchedRules(results.stream().map(EvaluationResult::ruleId).toList())
                            .processingTimeMs(Duration.between(startTime, Instant.now()).toMillis())
                            .build();

                    if (isFraud) {
                        return saveFraudEventsReactive(transaction, results, severity).thenReturn(response);
                    } else {
                        return Mono.just(response);
                    }
                })
                .doOnSuccess(response -> {
                    transactionProcessedCounter.increment();
                    if (response.isFraud()) {
                        fraudDetectionCounter.increment();
                    }
                    log.info("Fraud validation completed for transaction {} - isFraud: {}, riskScore: {}",
                            transaction.transactionId(), response.isFraud(), response.riskScore());
                })
                .doOnError(error -> log.error("Fraud validation failed for transaction {}",
                        transaction.transactionId(), error));
    }

    private Mono<Void> saveTransaction(TransactionDto transaction) {
        return transactionRepository.save(TransactionMapper.INSTANCE.mapToEntity(transaction))
                .then();
    }

    private Mono<List<EvaluationResult>> performFraudChecks(TransactionDto transaction) {
        return Mono.zip(
                evaluateRules(transaction),
                checkVelocity(transaction)
        ).map(tuple -> {
            List<EvaluationResult> ruleResults = new ArrayList<>(tuple.getT1());
            List<EvaluationResult> velocityResults = tuple.getT2();
            ruleResults.addAll(velocityResults);
            return ruleResults;
        });
    }

    private Mono<List<EvaluationResult>> evaluateRules(TransactionDto transaction) {
        return ruleCache.getActiveRule()
                .map(ruleDto -> {
                    List<RuleDefinition> rules = fraudEvaluator.getRules(ruleDto);
                    return fraudEvaluator.evaluateAllRules(rules, transaction);
                })
                .onErrorResume(ResourceNotFound.class, e -> {
                    log.warn("No active rules found, skipping rule evaluation");
                    return Mono.just(List.of());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<List<EvaluationResult>> checkVelocity(TransactionDto transaction) {
        if (!config.getFraud().getVelocity().isEnabled()) {
            return Mono.just(List.of());
        }
        return velocityCheckService.checkVelocity(transaction);
    }


    private Mono<Void> saveFraudEventsReactive(TransactionDto transaction,
                                               List<EvaluationResult> results,
                                               String severity) {
        List<FraudEntity> fraudEntities = results.stream()
                .map(result -> FraudEntity.builder()
                        .ruleId(result.ruleId())
                        .accountId(transaction.accountId())
                        .reason(result.description())
                        .type(result.ruleName())
                        .severity(severity)
                        .transactionDate(transaction.timeStamp().toInstant(ZoneOffset.UTC))
                        .transactionId(transaction.transactionId())
                        .detectedAt(Instant.now())
                        .build())
                .toList();

        return fraudRepository.saveAll(fraudEntities)
                .then()
                .doOnSuccess(v -> log.info("Saved {} fraud events for transaction {}", fraudEntities.size(), transaction.transactionId()));
    }

    public Flux<FraudEntity> getFlaggedItems() {
        return fraudRepository.findAll()
                .sort((a, b) -> b.getDetectedAt().compareTo(a.getDetectedAt()));
    }

    public Mono<FraudEntity> getFlaggedItem(final long id) {
        return fraudRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFound("Fraud item not found for id " + id)));
    }

    public Flux<FraudEntity> getFraudByAccountId(final long accountId) {
        return fraudRepository.findByAccountId(accountId);
    }

    public Flux<FraudEntity> getFraudBySeverity(final String severity) {
        return fraudRepository.findBySeverity(severity)
                .sort((a, b) -> b.getDetectedAt().compareTo(a.getDetectedAt()));
    }
}
