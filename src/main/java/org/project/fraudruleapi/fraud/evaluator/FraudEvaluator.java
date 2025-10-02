package org.project.fraudruleapi.fraud.evaluator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.RuleDefinition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.shared.exception.ConvertionException;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FraudEvaluator {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<RuleDefinition> getRules(final RuleDto ruleDto) {
        try {
            JsonNode rulesNode = ruleDto.getData().get("rules");

            return objectMapper.readValue(
                    rulesNode.traverse(),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException e) {
            throw new ConvertionException("Unable to converted rules");
        }
    }

    public boolean evaluateCondition(Condition cond, TransactionDto tx) {
        return switch (cond.type()) {
            case AND -> cond.operands().stream().allMatch(op -> {
                try {
                    return evaluateCondition(op, tx);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            case OR -> cond.operands().stream().anyMatch(op -> {
                try {
                    return evaluateCondition(op, tx);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            case NOT -> {
                if (cond.operands() == null || cond.operands().isEmpty()) yield false;
                yield !evaluateCondition(cond.operands().getFirst(), tx);
            }
            case EQUAL, EQUALS -> equalsOp(cond, tx);
            case GREATER_THAN -> compareNumeric(cond, tx) > 0;
            case GREAT_THAN_OR_EQUAL -> compareNumeric(cond, tx) >= 0;
            case LESS_THAN -> compareNumeric(cond, tx) < 0;
            case LESS_THAN_OR_EQUAL -> compareNumeric(cond, tx) <= 0;
            case INCLUDE -> includeOp(cond, tx);
            default -> throw new IllegalArgumentException("Unsupported condition type: " + cond.type());
        };
    }

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
        if (fieldVal == null) return -1;
        double fv;
        if (fieldVal instanceof Number) {
            fv = ((Number) fieldVal).doubleValue();
        } else {
            try {
                fv = Double.parseDouble(fieldVal.toString());
            } catch (Exception ex) {
                return -1;
            }
        }
        Double cv = parseToDouble(cond.value());
        return Double.compare(fv, cv);
    }

    private Double parseToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        String s = value.toString().replaceAll("[^0-9.\\-]", "");
        return s.isEmpty() ? 0.0 : Double.parseDouble(s);
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
            Field f = TransactionDto.class.getDeclaredField(normalized);
            f.setAccessible(true);
            return f.get(transaction);
        } catch (NoSuchFieldException e) {
            try {
                PropertyDescriptor pd = new PropertyDescriptor(normalized, TransactionDto.class);
                Method getter = pd.getReadMethod();
                if (getter != null) return getter.invoke(transaction);
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
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