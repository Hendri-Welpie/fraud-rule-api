package org.project.fraudruleapi.fraud.evaluator.strategy;

import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.model.TransactionDto;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractConditionEvaluator implements ConditionEvaluator {

    private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();

    protected Object getFieldValue(TransactionDto transaction, String fieldName) {
        if (fieldName == null) return null;
        String normalized = normalizeField(fieldName);

        try {
            Field field = FIELD_CACHE.computeIfAbsent(normalized, n -> {
                try {
                    Field f = TransactionDto.class.getDeclaredField(n);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException e) {
                    log.debug("Field not found: {}", n);
                    return null;
                }
            });

            if (field != null) {
                return field.get(transaction);
            }
        } catch (Exception e) {
            log.error("Failed to access field {} on TransactionDto", normalized, e);
        }

        log.warn("No such field found for {}", normalized);
        return null;
    }

    protected String normalizeField(String fieldName) {
        return switch (fieldName) {
            case "transferType", "transactionType", "transaction_type" -> "transactionType";
            case "transferAmount", "amount" -> "transferAmount";
            case "beneficiary_account", "beneficiaryAccount" -> "beneficiaryAccount";
            case "accountId", "account" -> "accountId";
            case "currency" -> "currency";
            case "transactionId", "transaction" -> "transactionId";
            default -> snakeToCamel(fieldName);
        };
    }

    protected String snakeToCamel(String value) {
        if (!value.contains("_")) return value;
        String[] parts = value.split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)))
                        .append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }

    protected Double parseToDouble(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot parse null to double");
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            log.error("Invalid numeric value: {}", value, ex);
            throw new IllegalArgumentException("Invalid numeric value: " + value, ex);
        }
    }
}

