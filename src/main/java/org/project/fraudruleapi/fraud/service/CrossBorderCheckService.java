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
public class CrossBorderCheckService {

    private static final String RULE_ID = "CROSS_BORDER_CHECK";

    private final ApplicationConfiguration config;

    public Mono<List<EvaluationResult>> checkCrossBorder(TransactionDto transaction) {
        return Mono.fromCallable(() -> {
            List<EvaluationResult> results = new ArrayList<>();
            var crossBorderConfig = config.getFraud().getCrossBorder();

            if (!crossBorderConfig.isEnabled()) {
                return results;
            }

            String domesticCurrency = crossBorderConfig.getDomesticCurrency();
            if (transaction.currency() != null && !transaction.currency().equalsIgnoreCase(domesticCurrency)) {
                log.warn("Cross-border transaction detected for account {}: currency {} (domestic: {})",
                        transaction.accountId(), transaction.currency(), domesticCurrency);

                results.add(EvaluationResult.builder()
                        .ruleId(RULE_ID)
                        .ruleName("Cross-Border Transaction Detection")
                        .description(String.format(
                                "Transaction uses foreign currency %s (domestic: %s) for account %d",
                                transaction.currency(), domesticCurrency, transaction.accountId()))
                        .matched(true)
                        .weight(crossBorderConfig.getWeight())
                        .evaluationTimeMs(0)
                        .build());
            }

            return results;
        });
    }
}

