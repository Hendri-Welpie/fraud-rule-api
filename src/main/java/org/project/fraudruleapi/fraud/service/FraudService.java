package org.project.fraudruleapi.fraud.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.evaluator.FraudEvaluator;
import org.project.fraudruleapi.fraud.mapper.TransactionMapper;
import org.project.fraudruleapi.fraud.model.PageResponse;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.fraud.repository.FraudRepository;
import org.project.fraudruleapi.fraud.repository.TransactionRepository;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class FraudService {
    private final RuleCache ruleCache;
    private final FraudEvaluator fraudEvaluator;
    private final FraudRepository fraudRepository;
    private final TransactionRepository transactionRepository;

    public Mono<Void> validate(@Valid TransactionDto transaction) {
        return Mono.fromCallable(() -> transactionRepository.save(
                        TransactionMapper.INSTANCE.mapToEntity(transaction)))
                .subscribeOn(Schedulers.boundedElastic())
                .then(ruleCache.getActiveRule()
                        .flatMapMany(ruleDto -> Flux.fromIterable(fraudEvaluator.getRules(ruleDto)))
                        .publishOn(Schedulers.boundedElastic())
                        .flatMap(rule -> {
                            boolean isMatched = fraudEvaluator.evaluateCondition(rule.condition(), transaction);

                            if (isMatched) {
                                return Mono.fromCallable(() -> fraudRepository.save(
                                                FraudEntity.builder()
                                                        .ruleId(rule.id())
                                                        .accountId(transaction.accountId())
                                                        .reason(rule.description())
                                                        .type(rule.name())
                                                        .transactionDate(transaction.timeStamp().toInstant(ZoneOffset.UTC))
                                                        .transactionId(transaction.transactionId())
                                                        .detectedAt(Instant.now())
                                                        .build()))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .then();
                            }
                            return Mono.empty();
                        })
                        .then());
    }

    public Mono<Page<FraudEntity>> getFlaggedItems(final int page, final int size) {
        return Mono.fromCallable(() -> fraudRepository.findAll(PageRequest.of(page, size)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FraudEntity> getFlaggedItem(final long id) {
        return Mono.fromCallable(() -> this.fraudRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .switchIfEmpty(Mono.error(new ResourceNotFound("Fraud item not found for id " + id)));
    }
}
