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
    private final CrossBorderCheckService crossBorderCheckService;
    private final SelfTransferCheckService selfTransferCheckService;
    private final HighValueCheckService highValueCheckService;
    private final OffHoursCheckService offHoursCheckService;
    private final Counter fraudDetectionCounter;
    private final Counter transactionProcessedCounter;

    @Transactional
    public Mono<FraudDetectionResponse> validate(@Valid TransactionDto transaction) {
        Instant startTime = Instant.now();

        return transactionRepository.existsByTransactionId(transaction.transactionId())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Duplicate transaction detected: {}", transaction.transactionId());
                        return Mono.just(FraudDetectionResponse.builder()
                                .transactionId(transaction.transactionId())
                                .isFraud(false)
                                .riskScore(0)
                                .severity("DUPLICATE")
                                .matchedRules(List.of())
                                .processingTimeMs(0L)
                                .build());
                    }
                    return processTransaction(transaction, startTime);
                });
    }

    private Mono<FraudDetectionResponse> processTransaction(TransactionDto transaction, Instant startTime) {
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
                checkVelocity(transaction),
                crossBorderCheckService.checkCrossBorder(transaction),
                selfTransferCheckService.checkSelfTransfer(transaction),
                highValueCheckService.checkHighValue(transaction),
                offHoursCheckService.checkOffHours(transaction)
        ).map(tuple -> {
            List<EvaluationResult> allResults = new ArrayList<>(tuple.getT1());
            allResults.addAll(tuple.getT2());
            allResults.addAll(tuple.getT3());
            allResults.addAll(tuple.getT4());
            allResults.addAll(tuple.getT5());
            allResults.addAll(tuple.getT6());
            return allResults;
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

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    public Flux<FraudEntity> getFlaggedItems(int page, int size) {
        int validSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int validPage = Math.max(page, 0);
        return fraudRepository.findAllPaginated(validSize, (long) validPage * validSize);
    }

    public Flux<FraudEntity> getFlaggedItems() {
        return getFlaggedItems(0, DEFAULT_PAGE_SIZE);
    }

    public Mono<FraudEntity> getFlaggedItem(final long id) {
        return fraudRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFound("Fraud item not found for id " + id)));
    }

    public Flux<FraudEntity> getFraudByAccountId(final long accountId) {
        return fraudRepository.findByAccountId(accountId);
    }

    public Flux<FraudEntity> getFraudBySeverity(final String severity, int page, int size) {
        int validSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int validPage = Math.max(page, 0);
        return fraudRepository.findBySeverityPaginated(severity, validSize, (long) validPage * validSize);
    }

    public Flux<FraudEntity> getFraudBySeverity(final String severity) {
        return getFraudBySeverity(severity, 0, DEFAULT_PAGE_SIZE);
    }
}
