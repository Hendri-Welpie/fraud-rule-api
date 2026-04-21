package org.project.fraudruleapi.fraud;

import org.junit.jupiter.api.Test;
import org.project.fraudruleapi.fraud.evaluator.strategy.*;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.enums.ConditionType;
import org.project.fraudruleapi.shared.enums.TransactionType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorStrategyTest {

    private final TransactionDto tx = TransactionDto.builder()
            .transactionId("TX1")
            .transactionType(TransactionType.TRANSFER)
            .transferAmount(500.0)
            .accountId(1001L)
            .beneficiaryAccount(2002L)
            .currency("USD")
            .build();

    @Test
    void notEquals_shouldReturnTrue_whenStringsDiffer() {
        var eval = new NotEqualsEvaluator();
        assertThat(eval.getSupportedType()).isEqualTo(ConditionType.NOT_EQUALS);
        assertTrue(eval.evaluate(new Condition(ConditionType.NOT_EQUALS, "currency", "ZAR", null), tx));
    }

    @Test
    void notEquals_shouldReturnFalse_whenStringsMatch() {
        var eval = new NotEqualsEvaluator();
        assertFalse(eval.evaluate(new Condition(ConditionType.NOT_EQUALS, "currency", "USD", null), tx));
    }

    @Test
    void notEquals_shouldReturnTrue_whenFieldNullAndValueNotNull() {
        var eval = new NotEqualsEvaluator();
        var txNoIp = TransactionDto.builder().build();
        assertTrue(eval.evaluate(new Condition(ConditionType.NOT_EQUALS, "ipAddress", "1.2.3.4", null), txNoIp));
    }

    @Test
    void notEquals_shouldReturnFalse_whenBothNull() {
        var eval = new NotEqualsEvaluator();
        var txNull = TransactionDto.builder().build();
        assertFalse(eval.evaluate(new Condition(ConditionType.NOT_EQUALS, "ipAddress", null, null), txNull));
    }

    @Test
    void notEquals_shouldCompareNumbers() {
        var eval = new NotEqualsEvaluator();
        assertTrue(eval.evaluate(new Condition(ConditionType.NOT_EQUALS, "transferAmount", 999.0, null), tx));
        assertFalse(eval.evaluate(new Condition(ConditionType.NOT_EQUALS, "transferAmount", 500.0, null), tx));
    }

    @Test
    void equals_shouldReturnTrue_whenBothNull() {
        var eval = new EqualsEvaluator();
        var txNull = TransactionDto.builder().build();
        assertTrue(eval.evaluate(new Condition(ConditionType.EQUALS, "ipAddress", null, null), txNull));
    }

    @Test
    void equals_shouldReturnFalse_whenFieldNull() {
        var eval = new EqualsEvaluator();
        var txNull = TransactionDto.builder().build();
        assertFalse(eval.evaluate(new Condition(ConditionType.EQUALS, "ipAddress", "1.2.3.4", null), txNull));
    }

    @Test
    void gte_shouldReturnTrue_whenEqual() {
        var eval = new GreaterThanOrEqualEvaluator();
        assertThat(eval.getSupportedType()).isEqualTo(ConditionType.GREATER_THAN_OR_EQUAL);
        assertTrue(eval.evaluate(new Condition(ConditionType.GREATER_THAN_OR_EQUAL, "transferAmount", 500.0, null), tx));
    }

    @Test
    void gte_shouldReturnTrue_whenGreater() {
        var eval = new GreaterThanOrEqualEvaluator();
        assertTrue(eval.evaluate(new Condition(ConditionType.GREATER_THAN_OR_EQUAL, "transferAmount", 100.0, null), tx));
    }

    @Test
    void gte_shouldReturnFalse_whenLess() {
        var eval = new GreaterThanOrEqualEvaluator();
        assertFalse(eval.evaluate(new Condition(ConditionType.GREATER_THAN_OR_EQUAL, "transferAmount", 1000.0, null), tx));
    }

    @Test
    void gte_shouldThrow_whenFieldNull() {
        var eval = new GreaterThanOrEqualEvaluator();
        var txNull = TransactionDto.builder().build();
        assertThrows(IllegalArgumentException.class,
                () -> eval.evaluate(new Condition(ConditionType.GREATER_THAN_OR_EQUAL, "transferAmount", 100.0, null), txNull));
    }

    @Test
    void lt_shouldReturnTrue_whenLess() {
        var eval = new LessThanEvaluator();
        assertThat(eval.getSupportedType()).isEqualTo(ConditionType.LESS_THAN);
        assertTrue(eval.evaluate(new Condition(ConditionType.LESS_THAN, "transferAmount", 1000.0, null), tx));
    }

    @Test
    void lt_shouldReturnFalse_whenEqual() {
        var eval = new LessThanEvaluator();
        assertFalse(eval.evaluate(new Condition(ConditionType.LESS_THAN, "transferAmount", 500.0, null), tx));
    }

    @Test
    void lt_shouldThrow_whenFieldNull() {
        var eval = new LessThanEvaluator();
        var txNull = TransactionDto.builder().build();
        assertThrows(IllegalArgumentException.class,
                () -> eval.evaluate(new Condition(ConditionType.LESS_THAN, "transferAmount", 100.0, null), txNull));
    }

    @Test
    void lte_shouldReturnTrue_whenEqual() {
        var eval = new LessThanOrEqualEvaluator();
        assertThat(eval.getSupportedType()).isEqualTo(ConditionType.LESS_THAN_OR_EQUAL);
        assertTrue(eval.evaluate(new Condition(ConditionType.LESS_THAN_OR_EQUAL, "transferAmount", 500.0, null), tx));
    }

    @Test
    void lte_shouldReturnFalse_whenGreater() {
        var eval = new LessThanOrEqualEvaluator();
        assertFalse(eval.evaluate(new Condition(ConditionType.LESS_THAN_OR_EQUAL, "transferAmount", 100.0, null), tx));
    }

    @Test
    void include_shouldReturnFalse_whenFieldNull() {
        var eval = new IncludeEvaluator();
        var txNull = TransactionDto.builder().build();
        assertFalse(eval.evaluate(new Condition(ConditionType.INCLUDE, "currency", List.of("USD"), null), txNull));
    }

    @Test
    void include_shouldHandleStringValue() {
        var eval = new IncludeEvaluator();
        assertTrue(eval.evaluate(new Condition(ConditionType.INCLUDE, "currency", "USD,EUR", null), tx));
    }

    @Test
    void include_shouldHandleJsonArrayString() {
        var eval = new IncludeEvaluator();
        assertTrue(eval.evaluate(new Condition(ConditionType.INCLUDE, "currency", "[\"USD\",\"EUR\"]", null), tx));
    }

    @Test
    void include_shouldReturnFalse_whenNotIncluded() {
        var eval = new IncludeEvaluator();
        assertFalse(eval.evaluate(new Condition(ConditionType.INCLUDE, "currency", List.of("ZAR", "EUR"), null), tx));
    }

    @Test
    void factory_getEvaluator_shouldReturnEmpty_forUnknownType() {
        var factory = new ConditionEvaluatorFactory(List.of(new EqualsEvaluator()));
        factory.init();
        assertThat(factory.getEvaluator(ConditionType.NOT_EQUALS)).isEmpty();
    }

    @Test
    void factory_getEvaluatorOrThrow_shouldThrow_forUnknownType() {
        var factory = new ConditionEvaluatorFactory(List.of(new EqualsEvaluator()));
        factory.init();
        assertThrows(IllegalArgumentException.class, () -> factory.getEvaluatorOrThrow(ConditionType.NOT_EQUALS));
    }

    @Test
    void factory_shouldHandleDuplicateRegistrations() {
        var factory = new ConditionEvaluatorFactory(List.of(new EqualsEvaluator(), new EqualsEvaluator()));
        factory.init();
        assertThat(factory.getEvaluator(ConditionType.EQUALS)).isPresent();
    }

    @Test
    void normalizeField_shouldHandleSnakeCase() {
        var eval = new EqualsEvaluator();
        assertTrue(eval.evaluate(new Condition(ConditionType.EQUALS, "beneficiary_account", 2002L, null), tx));
    }

    @Test
    void parseToDouble_shouldThrowOnNull() {
        var eval = new GreaterThanEvaluator();
        var txNull = TransactionDto.builder().build();
        assertThrows(IllegalArgumentException.class,
                () -> eval.evaluate(new Condition(ConditionType.GREATER_THAN, "transferAmount", 100, null), txNull));
    }
}
