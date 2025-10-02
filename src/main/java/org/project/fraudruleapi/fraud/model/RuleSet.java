package org.project.fraudruleapi.fraud.model;

import lombok.Builder;

import java.util.List;

@Builder
public record RuleSet(
        String createdBy,
        String createdAt,
        String updatedAt,
        List<RuleDefinition> rules) {
}