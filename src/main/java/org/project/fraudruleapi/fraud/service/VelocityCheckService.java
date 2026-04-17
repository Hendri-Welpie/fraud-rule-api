package org.project.fraudruleapi.fraud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.model.EvaluationResult;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.fraud.repository.TransactionRepository;
import org.project.fraudruleapi.shared.config.ApplicationConfiguration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VelocityCheckService {

    private static final String VELOCITY_KEY_PREFIX = "velocity:account:";
    private static final String VELOCITY_RULE_ID = "VELOCITY_CHECK";

    private final ApplicationConfiguration config;
    private final TransactionRepository transactionRepository;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public Mono<List<EvaluationResult>> checkVelocity(TransactionDto transaction) {
        var velocityConfig = config.getFraud().getVelocity();
        String key = VELOCITY_KEY_PREFIX + transaction.accountId();

        return incrementAndGetCount(key, velocityConfig.getWindowSeconds())
                .map(count -> {
                    List<EvaluationResult> results = new ArrayList<>();

                    if (count > velocityConfig.getMaxTransactions()) {
                        log.warn("Velocity limit exceeded for account {}: {} transactions in {}s window",
                                transaction.accountId(), count, velocityConfig.getWindowSeconds());

                        results.add(EvaluationResult.builder()
                                .ruleId(VELOCITY_RULE_ID)
                                .ruleName("Transaction Velocity Check")
                                .description(String.format(
                                        "Account %d exceeded velocity limit: %d transactions in %d seconds (max: %d)",
                                        transaction.accountId(),
                                        count,
                                        velocityConfig.getWindowSeconds(),
                                        velocityConfig.getMaxTransactions()))
                                .matched(true)
                                .evaluationTimeMs(0)
                                .build());
                    }

                    return results;
                })
                .onErrorResume(e -> {
                    log.error("Error checking velocity for account {}, falling back to DB check",
                            transaction.accountId(), e);
                    return checkVelocityFromDatabase(transaction);
                });
    }

    private Mono<Long> incrementAndGetCount(String key, int windowSeconds) {
        return reactiveRedisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        return reactiveRedisTemplate.expire(key, Duration.ofSeconds(windowSeconds))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                });
    }

    private Mono<List<EvaluationResult>> checkVelocityFromDatabase(TransactionDto transaction) {
        var velocityConfig = config.getFraud().getVelocity();
        Instant windowStart = Instant.now().minus(velocityConfig.getWindowSeconds(), ChronoUnit.SECONDS);

        return transactionRepository.countByAccountIdAndTimeStampAfter(transaction.accountId(), windowStart)
                .map(count -> {
                    List<EvaluationResult> results = new ArrayList<>();

                    if (count >= velocityConfig.getMaxTransactions()) {
                        results.add(EvaluationResult.builder()
                                .ruleId(VELOCITY_RULE_ID)
                                .ruleName("Transaction Velocity Check (DB Fallback)")
                                .description(String.format(
                                        "Account %d exceeded velocity limit: %d transactions in %d seconds",
                                        transaction.accountId(),
                                        count,
                                        velocityConfig.getWindowSeconds()))
                                .matched(true)
                                .evaluationTimeMs(0)
                                .build());
                    }

                    return results;
                });
    }
}
