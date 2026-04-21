package org.project.fraudruleapi.fraud.evaluator.strategy;

import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.enums.ConditionType;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class NotEqualsEvaluator extends AbstractConditionEvaluator {

    @Override
    public ConditionType getSupportedType() {
        return ConditionType.NOT_EQUALS;
    }

    @Override
    public boolean evaluate(Condition condition, TransactionDto transaction) {
        Object fieldVal = getFieldValue(transaction, condition.field());
        Object condVal = condition.value();

        if (fieldVal == null && condVal == null) return false;
        if (fieldVal == null) return true;

        if (fieldVal instanceof Number) {
            Double fieldValue = ((Number) fieldVal).doubleValue();
            Double conditionValue = parseToDouble(condVal);
            return !Objects.equals(fieldValue, conditionValue);
        } else {
            return !String.valueOf(fieldVal).equalsIgnoreCase(String.valueOf(condVal));
        }
    }
}

