package org.project.fraudruleapi.fraud.evaluator.strategy;

import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.enums.ConditionType;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BetweenEvaluator extends AbstractConditionEvaluator {

    @Override
    public ConditionType getSupportedType() {
        return ConditionType.BETWEEN;
    }

    @Override
    public boolean evaluate(Condition condition, TransactionDto transaction) {
        Object fieldVal = getFieldValue(transaction, condition.field());
        if (fieldVal == null) {
            throw new IllegalArgumentException("Field " + condition.field() + " is null, cannot compare numerically");
        }

        List<?> range = parseRange(condition.value());
        if (range.size() != 2) {
            throw new IllegalArgumentException("BETWEEN condition requires exactly 2 values [min, max], got: " + range.size());
        }

        double fieldValue = parseToDouble(fieldVal);
        double min = parseToDouble(range.get(0));
        double max = parseToDouble(range.get(1));

        return Double.compare(fieldValue, min) >= 0 && Double.compare(fieldValue, max) <= 0;
    }

    private List<?> parseRange(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        throw new IllegalArgumentException("BETWEEN condition value must be a list [min, max], got: " + value);
    }
}

