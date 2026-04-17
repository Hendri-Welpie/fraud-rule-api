package org.project.fraudruleapi.fraud.evaluator.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.enums.ConditionalType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IncludeEvaluator extends AbstractConditionEvaluator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConditionalType getSupportedType() {
        return ConditionalType.INCLUDE;
    }

    @Override
    public boolean evaluate(Condition condition, TransactionDto transaction) {
        Object fieldVal = getFieldValue(transaction, condition.field());
        if (fieldVal == null) return false;

        List<String> candidates = parseList(condition.value());
        String fieldValue = String.valueOf(fieldVal);
        return candidates.contains(fieldValue) || candidates.contains(removeQuotes(fieldValue));
    }

    private List<String> parseList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        String trimmedValue = value.toString().trim();
        if (trimmedValue.startsWith("[") && trimmedValue.endsWith("]")) {
            try {
                return objectMapper.readValue(trimmedValue, new TypeReference<>() {});
            } catch (Exception ex) {
                log.warn("Failed to parse JSON list, falling back to split: {}", trimmedValue, ex);
                trimmedValue = trimmedValue.substring(1, trimmedValue.length() - 1);
            }
        }
        return Arrays.stream(trimmedValue.split(","))
                .map(String::trim)
                .map(this::removeQuotes)
                .collect(Collectors.toList());
    }

    private String removeQuotes(String value) {
        return value.replaceAll("^\"|\"$", "");
    }
}

