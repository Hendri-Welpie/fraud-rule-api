package org.project.fraudruleapi.fraud.model;

import lombok.Builder;

import java.util.List;

@Builder
public record FraudDetectionResponse(
        String transactionId,
        boolean isFraud,
        int riskScore,
        String severity,
        List<String> matchedRules,
        long processingTimeMs
) {}

