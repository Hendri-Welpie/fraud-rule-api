package org.project.fraudruleapi.fraud.model;

import lombok.Builder;

@Builder
public record RuleDefinition(
        String id,
        String name,
        String description,
        Condition condition) {
}