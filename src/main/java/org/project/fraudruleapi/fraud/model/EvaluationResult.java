package org.project.fraudruleapi.fraud.model;

import lombok.Builder;

@Builder
public record EvaluationResult(
        String ruleId,
        String ruleName,
        String description,
        boolean matched,
        long evaluationTimeMs,
        int weight
) {}

