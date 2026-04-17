package org.project.fraudruleapi.fraud.evaluator.strategy;

import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.enums.ConditionalType;

public interface ConditionEvaluator {

    ConditionalType getSupportedType();

    boolean evaluate(Condition condition, TransactionDto transaction);
}

