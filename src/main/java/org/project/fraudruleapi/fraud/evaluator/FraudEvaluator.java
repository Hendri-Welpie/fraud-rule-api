package org.project.fraudruleapi.fraud.evaluator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.RuleDefinition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.shared.enums.ConditionalType;
import org.project.fraudruleapi.shared.exception.ConvertionException;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Slf4j
public class FraudEvaluator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Cache reflection results
    private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> GETTER_CACHE = new ConcurrentHashMap<>();

    // Operator strategy map
    private final Map<ConditionalType, BiPredicate<Condition, TransactionDto>> evaluators =
            Map.ofEntries(
                    Map.entry(ConditionalType.EQUAL, this::equalsOp),
                    Map.entry(ConditionalType.EQUALS, this::equalsOp),
                    Map.entry(ConditionalType.GREATER_THAN, (c, t) -> compareNumeric(c, t) > 0),
                    Map.entry(ConditionalType.GREAT_THAN_OR_EQUAL, (c, t) -> compareNumeric(c, t) >= 0),
                    Map.entry(ConditionalType.LESS_THAN, (c, t) -> compareNumeric(c, t) < 0),
                    Map.entry(ConditionalType.LESS_THAN_OR_EQUAL, (c, t) -> compareNumeric(c, t) <= 0),
                    Map.entry(ConditionalType.INCLUDE, this::includeOp)
            );

    public List<RuleDefinition> getRules(final RuleDto ruleDto) {
        try {
            JsonNode rulesNode = ruleDto.getData().get("rules");
            return objectMapper.readValue(
                    rulesNode.traverse(),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException e) {
            log.error("Failed to parse rules from RuleDto", e);
            throw new ConvertionException("Unable to convert rules");
        }
    }

    public boolean evaluateCondition(Condition cond, TransactionDto tx) {
        return switch (cond.type()) {
            case AND -> cond.operands().stream().allMatch(op -> evaluateCondition(op, tx));
            case OR -> cond.operands().stream().anyMatch(op -> evaluateCondition(op, tx));
            case NOT -> cond.operands() != null && !cond.operands().isEmpty()
                    && !evaluateCondition(cond.operands().getFirst(), tx);
            default -> {
                BiPredicate<Condition, TransactionDto> evaluator = evaluators.get(cond.type());
                if (evaluator == null) {
                    log.error("Unsupported condition type: {}", cond.type());
                    throw new IllegalArgumentException("Unsupported condition type: " + cond.type());
                }
                yield evaluator.test(cond, tx);
            }
        };
    }

    // ===== Operator implementations =====

    private boolean equalsOp(Condition cond, TransactionDto tx) {
        Object fieldVal = getFieldValue(tx, cond.field());
        Object condVal = cond.value();

        if (fieldVal == null && condVal == null) return true;
        if (fieldVal == null) return false;

        if (fieldVal instanceof Number) {
            Double fv = ((Number) fieldVal).doubleValue();
            Double cv = parseToDouble(condVal);
            return Objects.equals(fv, cv);
        } else {
            return String.valueOf(fieldVal).equalsIgnoreCase(String.valueOf(condVal));
        }
    }

    private boolean includeOp(Condition cond, TransactionDto tx) {
        Object fieldVal = getFieldValue(tx, cond.field());
        if (fieldVal == null) return false;

        List<String> candidates = parseList(cond.value());
        String fv = String.valueOf(fieldVal);
        return candidates.contains(fv) || candidates.contains(removeQuotes(fv));
    }

    private int compareNumeric(Condition cond, TransactionDto tx) {
        Object fieldVal = getFieldValue(tx, cond.field());
        if (fieldVal == null) {
            throw new IllegalArgumentException("Field " + cond.field() + " is null, cannot compare numerically");
        }

        double fv = parseToDouble(fieldVal);
        Double cv = parseToDouble(cond.value());
        return Double.compare(fv, cv);
    }

    // ===== Helper methods =====

    private Double parseToDouble(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot parse null to double");
        }
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            log.error("Invalid numeric value: {}", value, ex);
            throw new IllegalArgumentException("Invalid numeric value: " + value, ex);
        }
    }

    private List<String> parseList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof List) {
            return ((List<?>) value).stream().map(Object::toString).collect(Collectors.toList());
        }
        String s = value.toString().trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            try {
                return objectMapper.readValue(s, new TypeReference<>() {
                });
            } catch (Exception ex) {
                log.warn("Failed to parse JSON list, falling back to split: {}", s, ex);
                s = s.substring(1, s.length() - 1);
            }
        }
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .map(this::removeQuotes)
                .collect(Collectors.toList());
    }

    private String removeQuotes(String s) {
        return s.replaceAll("^\"|\"$", "");
    }

    private Object getFieldValue(TransactionDto transaction, String fieldName) {
        if (fieldName == null) return null;
        String normalized = normalizeField(fieldName);

        try {
            Field f = FIELD_CACHE.computeIfAbsent(normalized, n -> {
                try {
                    Field field = TransactionDto.class.getDeclaredField(n);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException e) {
                    return null;
                }
            });
            if (f != null) {
                return f.get(transaction);
            }

            Method getter = GETTER_CACHE.computeIfAbsent(normalized, n -> {
                try {
                    PropertyDescriptor pd = new PropertyDescriptor(n, TransactionDto.class);
                    return pd.getReadMethod();
                } catch (Exception ex) {
                    return null;
                }
            });
            if (getter != null) {
                return getter.invoke(transaction);
            }
        } catch (Exception e) {
            log.error("Failed to access field {} on TransactionDto", normalized, e);
            return null;
        }
        log.warn("No such field/getter found for {}", normalized);
        return null;
    }

    private String normalizeField(String fieldName) {
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

    private String snakeToCamel(String value) {
        if (!value.contains("_")) return value;
        String[] parts = value.split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return sb.toString();
    }
}