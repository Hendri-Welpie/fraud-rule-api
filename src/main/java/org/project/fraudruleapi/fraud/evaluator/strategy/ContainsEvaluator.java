package org.project.fraudruleapi.fraud.evaluator.strategy;

import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.enums.ConditionType;
import org.springframework.stereotype.Component;

@Component
public class ContainsEvaluator extends AbstractConditionEvaluator {

    @Override
    public ConditionType getSupportedType() {
        return ConditionType.CONTAINS;
    }

    @Override
    public boolean evaluate(Condition condition, TransactionDto transaction) {
        Object fieldVal = getFieldValue(transaction, condition.field());
        if (fieldVal == null) return false;

        String fieldValue = String.valueOf(fieldVal).toLowerCase();
        String searchValue = String.valueOf(condition.value()).toLowerCase();
        return fieldValue.contains(searchValue);
    }
}

