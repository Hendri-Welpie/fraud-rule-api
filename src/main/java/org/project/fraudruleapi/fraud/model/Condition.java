package org.project.fraudruleapi.fraud.model;

import lombok.Builder;
import org.project.fraudruleapi.shared.enums.ConditionalType;

import java.util.List;

@Builder
public record Condition(
        ConditionalType type,
        String field,
        Object value,
        List<Condition> operands) {
}
