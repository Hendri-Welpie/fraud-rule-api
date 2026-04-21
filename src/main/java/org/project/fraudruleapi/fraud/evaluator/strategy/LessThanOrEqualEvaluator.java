package org.project.fraudruleapi.fraud.evaluator.strategy;

import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.enums.ConditionType;
import org.springframework.stereotype.Component;

@Component
public class LessThanOrEqualEvaluator extends AbstractConditionEvaluator {

    @Override
    public ConditionType getSupportedType() {
        return ConditionType.LESS_THAN_OR_EQUAL;
    }

    @Override
    public boolean evaluate(Condition condition, TransactionDto transaction) {
        Object fieldVal = getFieldValue(transaction, condition.field());
        if (fieldVal == null) {
            throw new IllegalArgumentException("Field " + condition.field() + " is null, cannot compare numerically");
        }

        double fieldValue = parseToDouble(fieldVal);
        double conditionValue = parseToDouble(condition.value());
        return Double.compare(fieldValue, conditionValue) <= 0;
    }
}

