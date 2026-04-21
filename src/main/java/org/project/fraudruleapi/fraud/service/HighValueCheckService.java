package org.project.fraudruleapi.fraud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.model.EvaluationResult;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.config.ApplicationConfiguration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HighValueCheckService {

    private static final String HIGH_VALUE_RULE_ID = "HIGH_VALUE_CHECK";
    private static final String SUSPICIOUS_AMOUNT_RULE_ID = "SUSPICIOUS_AMOUNT_CHECK";

    private final ApplicationConfiguration config;

    public Mono<List<EvaluationResult>> checkHighValue(TransactionDto transaction) {
        return Mono.fromCallable(() -> {
            List<EvaluationResult> results = new ArrayList<>();
            var amountConfig = config.getFraud().getAmount();

            if (transaction.transferAmount() == null) {
                return results;
            }

            double amount = transaction.transferAmount();
            double highValueThreshold = amountConfig.getHighValueThreshold().doubleValue();
            double suspiciousThreshold = amountConfig.getSuspiciousThreshold().doubleValue();

            if (amount >= highValueThreshold) {
                log.warn("High-value transaction detected for account {}: amount {} exceeds threshold {}",
                        transaction.accountId(), amount, highValueThreshold);

                results.add(EvaluationResult.builder()
                        .ruleId(HIGH_VALUE_RULE_ID)
                        .ruleName("High-Value Transaction Detection")
                        .description(String.format(
                                "Transaction amount %.2f exceeds high-value threshold %.2f for account %d",
                                amount, highValueThreshold, transaction.accountId()))
                        .matched(true)
                        .weight(35)
                        .evaluationTimeMs(0)
                        .build());
            } else if (amount >= suspiciousThreshold) {
                log.info("Suspicious amount transaction detected for account {}: amount {} exceeds suspicious threshold {}",
                        transaction.accountId(), amount, suspiciousThreshold);

                results.add(EvaluationResult.builder()
                        .ruleId(SUSPICIOUS_AMOUNT_RULE_ID)
                        .ruleName("Suspicious Amount Detection")
                        .description(String.format(
                                "Transaction amount %.2f exceeds suspicious threshold %.2f for account %d",
                                amount, suspiciousThreshold, transaction.accountId()))
                        .matched(true)
                        .weight(20)
                        .evaluationTimeMs(0)
                        .build());
            }

            return results;
        });
    }
}

