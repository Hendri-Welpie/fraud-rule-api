package org.project.fraudruleapi.fraud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.project.fraudruleapi.fraud.evaluator.FraudEvaluator;
import org.project.fraudruleapi.fraud.evaluator.strategy.*;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.config.ApplicationConfiguration;
import org.project.fraudruleapi.shared.enums.ConditionalType;
import org.project.fraudruleapi.shared.enums.TransactionType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FraudEvaluatorNumericTest {

    private FraudEvaluator evaluator;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        // Create real evaluators
        List<ConditionEvaluator> evaluators = List.of(
                new EqualsEvaluator(),
                new EqualEvaluator(),
                new GreaterThanEvaluator(),
                new GreaterThanOrEqualEvaluator(),
                new LessThanEvaluator(),
                new LessThanOrEqualEvaluator(),
                new IncludeEvaluator(),
                new NotEqualEvaluator(),
                new NotEqualsEvaluator()
        );

        ConditionEvaluatorFactory factory = new ConditionEvaluatorFactory(evaluators);
        factory.init();

        // Create config
        ApplicationConfiguration config = new ApplicationConfiguration();
        ApplicationConfiguration.FraudConfiguration fraudConfig = new ApplicationConfiguration.FraudConfiguration();
        ApplicationConfiguration.EvaluationConfig evaluationConfig = new ApplicationConfiguration.EvaluationConfig();
        evaluationConfig.setParallelThreshold(5);
        evaluationConfig.setTimeoutMs(5000L);
        fraudConfig.setEvaluation(evaluationConfig);
        config.setFraud(fraudConfig);

        evaluator = new FraudEvaluator(factory, config);

        transactionDto = TransactionDto.builder()
                .transactionId("TX123")
                .transactionType(TransactionType.TRANSFER)
                .transferAmount(1000.0)
                .currency("USD").build();
    }

    @ParameterizedTest(name = "{0} {1} {2} => expected={3}")
    @CsvSource({
            "GREATER_THAN, transferAmount, 500, true",
            "GREATER_THAN, transferAmount, 2000, false",
            "GREAT_THAN_OR_EQUAL, transferAmount, 1000, true",
            "GREAT_THAN_OR_EQUAL, transferAmount, 999, true",
            "LESS_THAN, transferAmount, 1500, true",
            "LESS_THAN, transferAmount, 500, false",
            "LESS_THAN_OR_EQUAL, transferAmount, 1000, true",
            "LESS_THAN_OR_EQUAL, transferAmount, 999, false"
    })
    void testNumericComparisons(String type, String field, double value, boolean expected) {
        ConditionalType conditionType = ConditionalType.valueOf(type);
        Condition cond = new Condition(conditionType, field, value, null);

        boolean result = evaluator.evaluateCondition(cond, transactionDto);

        assertEquals(expected, result, () ->
                String.format("Expected %s %s %s => %s", field, type, value, expected));
    }
}
