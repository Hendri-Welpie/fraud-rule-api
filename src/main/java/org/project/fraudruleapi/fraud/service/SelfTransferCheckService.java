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
public class SelfTransferCheckService {

    private static final String RULE_ID = "SELF_TRANSFER_CHECK";

    private final ApplicationConfiguration config;

    public Mono<List<EvaluationResult>> checkSelfTransfer(TransactionDto transaction) {
        return Mono.fromCallable(() -> {
            List<EvaluationResult> results = new ArrayList<>();

            if (!config.getFraud().getSelfTransfer().isEnabled()) {
                return results;
            }

            if (transaction.accountId() != null && transaction.beneficiaryAccount() != null
                    && transaction.accountId().equals(transaction.beneficiaryAccount())) {
                log.warn("Self-transfer detected for account {}: sending to own account",
                        transaction.accountId());

                results.add(EvaluationResult.builder()
                        .ruleId(RULE_ID)
                        .ruleName("Self-Transfer Detection")
                        .description(String.format(
                                "Account %d is transferring to itself (beneficiary: %d)",
                                transaction.accountId(), transaction.beneficiaryAccount()))
                        .matched(true)
                        .weight(config.getFraud().getSelfTransfer().getWeight())
                        .evaluationTimeMs(0)
                        .build());
            }

            return results;
        });
    }
}

