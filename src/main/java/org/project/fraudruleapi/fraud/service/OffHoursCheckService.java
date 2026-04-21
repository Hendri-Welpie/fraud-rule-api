package org.project.fraudruleapi.fraud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.model.EvaluationResult;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.config.ApplicationConfiguration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OffHoursCheckService {

    private static final String RULE_ID = "OFF_HOURS_CHECK";

    private final ApplicationConfiguration config;

    public Mono<List<EvaluationResult>> checkOffHours(TransactionDto transaction) {
        return Mono.fromCallable(() -> {
            List<EvaluationResult> results = new ArrayList<>();
            var offHoursConfig = config.getFraud().getOffHours();

            if (!offHoursConfig.isEnabled()) {
                return results;
            }

            if (transaction.timeStamp() == null) {
                return results;
            }

            LocalDateTime txTime = transaction.timeStamp();
            int hour = txTime.getHour();
            int startHour = offHoursConfig.getBusinessStartHour();
            int endHour = offHoursConfig.getBusinessEndHour();

            if (hour < startHour || hour >= endHour) {
                log.warn("Off-hours transaction detected for account {}: transaction at {}:00 (business hours: {}:00 - {}:00)",
                        transaction.accountId(), hour, startHour, endHour);

                results.add(EvaluationResult.builder()
                        .ruleId(RULE_ID)
                        .ruleName("Off-Hours Transaction Detection")
                        .description(String.format(
                                "Transaction at %02d:00 is outside business hours (%02d:00 - %02d:00) for account %d",
                                hour, startHour, endHour, transaction.accountId()))
                        .matched(true)
                        .weight(offHoursConfig.getWeight())
                        .evaluationTimeMs(0)
                        .build());
            }

            return results;
        });
    }
}

