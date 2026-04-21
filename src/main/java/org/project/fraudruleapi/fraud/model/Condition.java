package org.project.fraudruleapi.fraud.model;

import lombok.Builder;
import org.project.fraudruleapi.shared.enums.ConditionType;

import java.util.List;

@Builder
public record Condition(
        ConditionType type,
        String field,
        Object value,
        List<Condition> operands) {
}
