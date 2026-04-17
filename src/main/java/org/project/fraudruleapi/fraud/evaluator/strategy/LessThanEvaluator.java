package org.project.fraudruleapi.fraud.evaluator.strategy;

import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.enums.ConditionalType;
import org.springframework.stereotype.Component;

@Component
public class LessThanEvaluator extends AbstractConditionEvaluator {

    @Override
    public ConditionalType getSupportedType() {
        return ConditionalType.LESS_THAN;
    }

    @Override
    public boolean evaluate(Condition condition, TransactionDto transaction) {
        Object fieldVal = getFieldValue(transaction, condition.field());
        if (fieldVal == null) {
            throw new IllegalArgumentException("Field " + condition.field() + " is null, cannot compare numerically");
        }

        double fieldValue = parseToDouble(fieldVal);
        double conditionValue = parseToDouble(condition.value());
        return Double.compare(fieldValue, conditionValue) < 0;
    }
}

