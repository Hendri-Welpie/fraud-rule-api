package org.project.fraudruleapi.fraud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.fraudruleapi.fraud.evaluator.FraudEvaluator;
import org.project.fraudruleapi.fraud.evaluator.strategy.*;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.EvaluationResult;
import org.project.fraudruleapi.fraud.model.RuleDefinition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.shared.config.ApplicationConfiguration;
import org.project.fraudruleapi.shared.enums.ChannelType;
import org.project.fraudruleapi.shared.enums.ConditionType;
import org.project.fraudruleapi.shared.enums.StatusType;
import org.project.fraudruleapi.shared.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class FraudEvaluatorTest {

    private FraudEvaluator fraudEvaluator;
    private ObjectMapper objectMapper;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        List<ConditionEvaluator> evaluators = List.of(
                new EqualsEvaluator(),
                new GreaterThanEvaluator(),
                new GreaterThanOrEqualEvaluator(),
                new LessThanEvaluator(),
                new LessThanOrEqualEvaluator(),
                new IncludeEvaluator(),
                new NotEqualsEvaluator()
        );

        ConditionEvaluatorFactory factory = new ConditionEvaluatorFactory(evaluators);
        factory.init();

        ApplicationConfiguration config = new ApplicationConfiguration();
        ApplicationConfiguration.FraudConfiguration fraudConfig = new ApplicationConfiguration.FraudConfiguration();
        ApplicationConfiguration.EvaluationConfig evaluationConfig = new ApplicationConfiguration.EvaluationConfig();
        evaluationConfig.setParallelThreshold(5);
        evaluationConfig.setTimeoutMs(5000L);
        fraudConfig.setEvaluation(evaluationConfig);
        config.setFraud(fraudConfig);

        fraudEvaluator = new FraudEvaluator(factory, config);
        objectMapper = new ObjectMapper();

        transactionDto = TransactionDto.builder()
                .transactionId("TX123")
                .transactionType(TransactionType.TRANSFER)
                .transferAmount(1000.0)
                .beneficiaryAccount(346546456L)
                .accountId(76575675L)
                .currency("USD")
                .build();
    }

    @Test
    void getRules_shouldParseRulesFromJsonNode() throws Exception {
        String json = """
                {
                    "rules": [
                      {
                           "id": "rule-0001",
                           "name": "High value transfer to new beneficiary",
                           "description": "Block transfers >  1000000.00 to a new beneficiary",
                           "condition": {
                             "type": "AND",
                             "operands": [
                               {
                                 "type": "EQUALS",
                                 "field": "transferType",
                                 "value": "TRANSFER"
                               },
                               {
                                 "type": "GREATER_THAN",
                                 "field": "transferAmount",
                                 "value": 1000000.00
                               },
                               {
                                 "type": "EQUALS",
                                 "field": "currency",
                                 "value": "ZAR"
                               }
                             ]
                           }
                         },
                         {
                           "id": "rule-0002",
                           "name": "Block accounts for beneficiary",
                           "description": "Block transfer for certain blacklisted beneficiaries",
                           "condition": {
                             "type": "AND",
                             "operands": [
                               {
                                 "type": "EQUALS",
                                 "field": "transactionType",
                                 "value": "TRANSFER"
                               },
                               {
                                 "type": "EQUALS",
                                 "field": "currency",
                                 "value": "ZAR"
                               },
                               {
                                 "type": "INCLUDE",
                                 "field": "beneficiary_account",
                                 "value": ["32142347", "57437955", "80923904"]
                               }
                             ]
                           }
                         }
                    ]
                }
                """;

        RuleDto ruleDto = RuleDto.builder()
                .data(objectMapper.readTree(json))
                .build();

        List<?> rules = fraudEvaluator.getRules(ruleDto);

        assertThat(rules).isNotEmpty();
        assertThat(rules.size()).isEqualTo(2);
    }

    @Test
    void getRules_shouldReturnEmptyList_whenInvalidJson() {
        RuleDto ruleDto = RuleDto.builder()
                .data(mock(JsonNode.class))
                .build();

        List<?> rules = fraudEvaluator.getRules(ruleDto);
        assertThat(rules).isEmpty();
    }

    @Test
    void evaluateCondition_equals_shouldReturnTrueForMatchingString() {
        TransactionDto tx = TransactionDto.builder()
                .transactionType(TransactionType.TRANSFER)
                .build();

        Condition cond = new Condition(ConditionType.EQUALS, "transactionType", "transfer", null);

        boolean result = fraudEvaluator.evaluateCondition(cond, tx);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateCondition_equals_shouldReturnFalseForNonMatchingString() {
        TransactionDto tx = TransactionDto.builder()
                .transactionType(TransactionType.P2P_PAYMENT)
                .build();

        Condition cond = new Condition(ConditionType.EQUALS, "transactionType", "transfer", null);

        boolean result = fraudEvaluator.evaluateCondition(cond, tx);

        assertThat(result).isFalse();
    }

    @Test
    void evaluateCondition_greaterThan_shouldReturnTrue() {
        TransactionDto tx = TransactionDto.builder()
                .transferAmount(1000.0D)
                .build();

        Condition cond = new Condition(ConditionType.GREATER_THAN, "transferAmount", 100, null);

        boolean result = fraudEvaluator.evaluateCondition(cond, tx);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateCondition_include_shouldReturnTrue() {
        TransactionDto tx = TransactionDto.builder()
                .currency("USD")
                .build();

        Condition cond = new Condition(ConditionType.INCLUDE, "currency", List.of("USD", "EUR"), null);

        boolean result = fraudEvaluator.evaluateCondition(cond, tx);

        assertThat(result).isTrue();
    }

    @Test
    void evaluateCondition_andOrNot_shouldEvaluateCorrectly() {
        TransactionDto tx = TransactionDto.builder()
                .transferAmount(150.0)
                .currency("USD")
                .build();

        Condition cond1 = new Condition(ConditionType.GREATER_THAN, "transferAmount", 100, null);
        Condition cond2 = new Condition(ConditionType.EQUALS, "currency", "USD", null);
        Condition andCond = new Condition(ConditionType.AND, null, null, List.of(cond1, cond2));

        boolean result = fraudEvaluator.evaluateCondition(andCond, tx);

        assertThat(result).isTrue();

        Condition notCond = new Condition(ConditionType.NOT, null, null, List.of(cond2));
        boolean notResult = fraudEvaluator.evaluateCondition(notCond, tx);
        assertThat(notResult).isFalse();
    }


    @Test
    void testEqualsOperator_StringMatch() {
        Condition cond = new Condition(ConditionType.EQUALS, "transactionType", "TRANSFER", null);
        assertTrue(fraudEvaluator.evaluateCondition(cond, transactionDto));
    }

    @Test
    void testEqualsOperator_NumberMatch() {
        Condition cond = new Condition(ConditionType.EQUALS, "transferAmount", 1000.0, null);
        assertTrue(fraudEvaluator.evaluateCondition(cond, transactionDto));
    }

    @Test
    void testGreaterThan() {
        Condition cond = new Condition(ConditionType.GREATER_THAN, "transferAmount", 500, null);
        assertTrue(fraudEvaluator.evaluateCondition(cond, transactionDto));
    }

    @Test
    void testLessThanOrEqual() {
        Condition cond = new Condition(ConditionType.LESS_THAN_OR_EQUAL, "transferAmount", 1000.0, null);
        assertTrue(fraudEvaluator.evaluateCondition(cond, transactionDto));
    }

    @Test
    void testIncludeOperator() {
        Condition cond = new Condition(ConditionType.INCLUDE, "currency", List.of("USD", "ZAR"), null);
        assertTrue(fraudEvaluator.evaluateCondition(cond, transactionDto));
    }

    @Test
    void testAndOperator() {
        Condition cond1 = new Condition(ConditionType.EQUALS, "transactionType", "TRANSFER", null);
        Condition cond2 = new Condition(ConditionType.GREATER_THAN, "transferAmount", 500, null);
        Condition and = new Condition(ConditionType.AND, null, null, List.of(cond1, cond2));

        assertTrue(fraudEvaluator.evaluateCondition(and, transactionDto));
    }

    @Test
    void testOrOperator() {
        Condition cond1 = new Condition(ConditionType.EQUALS, "transactionType", "DEPOSIT", null);
        Condition cond2 = new Condition(ConditionType.EQUALS, "currency", "USD", null);
        Condition or = new Condition(ConditionType.OR, null, null, List.of(cond1, cond2));

        assertTrue(fraudEvaluator.evaluateCondition(or, transactionDto));
    }

    @Test
    void testNotOperator() {
        Condition cond = new Condition(ConditionType.NOT, null, null,
                List.of(new Condition(ConditionType.EQUALS, "currency", "ZAR", null)));

        assertTrue(fraudEvaluator.evaluateCondition(cond, transactionDto));
    }

    @Test
    void testInvalidNumericThrows() {
        Condition cond = new Condition(ConditionType.GREATER_THAN, "transactionType", "ABC", null);
        assertThrows(IllegalArgumentException.class, () -> fraudEvaluator.evaluateCondition(cond, transactionDto));
    }

    @Test
    void testMissingFieldReturnsNull() {
        Condition cond = new Condition(ConditionType.EQUALS, "nonexistentField", "value", null);
        assertFalse(fraudEvaluator.evaluateCondition(cond, transactionDto));
    }

    @Test
    void testParallelEvaluation() {
        ApplicationConfiguration config = new ApplicationConfiguration();
        ApplicationConfiguration.FraudConfiguration fraudConfig = new ApplicationConfiguration.FraudConfiguration();
        ApplicationConfiguration.EvaluationConfig evaluationConfig = new ApplicationConfiguration.EvaluationConfig();
        evaluationConfig.setParallelThreshold(1);
        evaluationConfig.setTimeoutMs(1000);
        fraudConfig.setEvaluation(evaluationConfig);
        config.setFraud(fraudConfig);

        ConditionEvaluatorFactory factory = new ConditionEvaluatorFactory(List.of(
                new EqualsEvaluator(),
                new GreaterThanEvaluator(),
                new GreaterThanOrEqualEvaluator(),
                new LessThanEvaluator(),
                new LessThanOrEqualEvaluator(),
                new IncludeEvaluator(),
                new NotEqualsEvaluator()
        ));
        factory.init();

        FraudEvaluator parallelEvaluator = new FraudEvaluator(factory, config);

        List<RuleDefinition> rules = List.of(
                RuleDefinition.builder()
                        .id("rule1")
                        .name("Rule 1")
                        .description("Test rule 1")
                        .condition(new Condition(ConditionType.EQUALS, "transferAmount", 1000.0, null))
                        .build(),
                RuleDefinition.builder()
                        .id("rule2")
                        .name("Rule 2")
                        .description("Test rule 2")
                        .condition(new Condition(ConditionType.EQUALS, "transferAmount", 2000.0, null))
                        .build()
        );

        List<EvaluationResult> results = parallelEvaluator.evaluateAllRules(rules, transactionDto);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().ruleId()).isEqualTo("rule1");
    }

    @Test
    void testAndCondition() {
        Condition andCond = new Condition(ConditionType.AND, null, null,
                List.of(
                        new Condition(ConditionType.EQUALS, "transferAmount", 1000.0, null),
                        new Condition(ConditionType.EQUALS, "currency", "USD", null)
                ));

        assertTrue(fraudEvaluator.evaluateCondition(andCond, transactionDto));
    }

    @Test
    void testOrCondition() {
        Condition orCond = new Condition(ConditionType.OR, null, null,
                List.of(
                        new Condition(ConditionType.EQUALS, "transferAmount", 1000.0, null),
                        new Condition(ConditionType.EQUALS, "transferAmount", 2000.0, null)
                ));

        assertTrue(fraudEvaluator.evaluateCondition(orCond, transactionDto));
    }

    @Test
    void testNotCondition() {
        Condition notCond = new Condition(ConditionType.NOT, null, null,
                List.of(new Condition(ConditionType.EQUALS, "transferAmount", 2000.0, null)));

        assertTrue(fraudEvaluator.evaluateCondition(notCond, transactionDto));
    }

    @Test
    void testEmptyAndCondition() {
        Condition andCond = new Condition(ConditionType.AND, null, null, List.of());
        assertTrue(fraudEvaluator.evaluateCondition(andCond, transactionDto));
    }

    @Test
    void testEmptyOrCondition() {
        Condition orCond = new Condition(ConditionType.OR, null, null, List.of());
        assertFalse(fraudEvaluator.evaluateCondition(orCond, transactionDto));
    }

    @Test
    void testEmptyNotCondition() {
        Condition notCond = new Condition(ConditionType.NOT, null, null, List.of());
        assertTrue(fraudEvaluator.evaluateCondition(notCond, transactionDto));
    }

    private final TransactionDto tx = new TransactionDto(
            "tx1", 1L, 10L, "USD", 750000.0,
            LocalDateTime.now(), TransactionType.WIRE_TRANSFER,
            ChannelType.WEB, "M001", "Crypto Exchange",
            200L, "192.168.1.50", "device1", "New York",
            StatusType.PENDING
    );

    @Test
    void between_shouldReturnTrue_whenValueInRange() {
        var evaluator = new BetweenEvaluator();
        assertThat(evaluator.getSupportedType()).isEqualTo(ConditionType.BETWEEN);
        var cond = new Condition(ConditionType.BETWEEN, "transferAmount", List.of(500000, 1000000), null);
        assertTrue(evaluator.evaluate(cond, tx));
    }

    @Test
    void between_shouldReturnFalse_whenValueOutOfRange() {
        var evaluator = new BetweenEvaluator();
        var cond = new Condition(ConditionType.BETWEEN, "transferAmount", List.of(800000, 1000000), null);
        assertFalse(evaluator.evaluate(cond, tx));
    }

    @Test
    void between_shouldReturnTrue_whenValueEqualsMin() {
        var evaluator = new BetweenEvaluator();
        var cond = new Condition(ConditionType.BETWEEN, "transferAmount", List.of(750000, 1000000), null);
        assertTrue(evaluator.evaluate(cond, tx));
    }

    @Test
    void between_shouldReturnTrue_whenValueEqualsMax() {
        var evaluator = new BetweenEvaluator();
        var cond = new Condition(ConditionType.BETWEEN, "transferAmount", List.of(500000, 750000), null);
        assertTrue(evaluator.evaluate(cond, tx));
    }

    @Test
    void between_shouldThrow_whenNullField() {
        var evaluator = new BetweenEvaluator();
        var cond = new Condition(ConditionType.BETWEEN, "nonExistentField", List.of(1, 2), null);
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(cond, tx));
    }

    @Test
    void between_shouldThrow_whenWrongRangeSize() {
        var evaluator = new BetweenEvaluator();
        var cond = new Condition(ConditionType.BETWEEN, "transferAmount", List.of(1), null);
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(cond, tx));
    }

    @Test
    void between_shouldThrow_whenValueNotList() {
        var evaluator = new BetweenEvaluator();
        var cond = new Condition(ConditionType.BETWEEN, "transferAmount", "notAList", null);
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(cond, tx));
    }

    @Test
    void regex_shouldReturnTrue_whenMatches() {
        var evaluator = new RegexEvaluator();
        assertThat(evaluator.getSupportedType()).isEqualTo(ConditionType.REGEX);
        var cond = new Condition(ConditionType.REGEX, "ipAddress", "192\\.168\\..*", null);
        assertTrue(evaluator.evaluate(cond, tx));
    }

    @Test
    void regex_shouldReturnFalse_whenNoMatch() {
        var evaluator = new RegexEvaluator();
        var cond = new Condition(ConditionType.REGEX, "ipAddress", "10\\.0\\..*", null);
        assertFalse(evaluator.evaluate(cond, tx));
    }

    @Test
    void regex_shouldReturnFalse_whenNullField() {
        var evaluator = new RegexEvaluator();
        var txNull = TransactionDto.builder().build();
        var cond = new Condition(ConditionType.REGEX, "ipAddress", ".*", null);
        assertFalse(evaluator.evaluate(cond, txNull));
    }

    @Test
    void regex_shouldReturnFalse_whenInvalidPattern() {
        var evaluator = new RegexEvaluator();
        var cond = new Condition(ConditionType.REGEX, "ipAddress", "[invalid", null);
        assertFalse(evaluator.evaluate(cond, tx));
    }

    @Test
    void regex_shouldReturnFalse_whenPatternTooLong() {
        var evaluator = new RegexEvaluator();
        String longPattern = "a".repeat(300);
        var cond = new Condition(ConditionType.REGEX, "ipAddress", longPattern, null);
        assertFalse(evaluator.evaluate(cond, tx));
    }

    @Test
    void startsWith_shouldReturnTrue_whenMatches() {
        var evaluator = new StartsWithEvaluator();
        assertThat(evaluator.getSupportedType()).isEqualTo(ConditionType.STARTS_WITH);
        var cond = new Condition(ConditionType.STARTS_WITH, "ipAddress", "192.168.", null);
        assertTrue(evaluator.evaluate(cond, tx));
    }

    @Test
    void startsWith_shouldReturnFalse_whenNoMatch() {
        var evaluator = new StartsWithEvaluator();
        var cond = new Condition(ConditionType.STARTS_WITH, "ipAddress", "10.", null);
        assertFalse(evaluator.evaluate(cond, tx));
    }

    @Test
    void startsWith_shouldReturnFalse_whenNullField() {
        var evaluator = new StartsWithEvaluator();
        var txNull = TransactionDto.builder().build();
        var cond = new Condition(ConditionType.STARTS_WITH, "ipAddress", "10.", null);
        assertFalse(evaluator.evaluate(cond, txNull));
    }

    @Test
    void endsWith_shouldReturnTrue_whenMatches() {
        var evaluator = new EndsWithEvaluator();
        assertThat(evaluator.getSupportedType()).isEqualTo(ConditionType.ENDS_WITH);
        var cond = new Condition(ConditionType.ENDS_WITH, "currency", "SD", null);
        assertTrue(evaluator.evaluate(cond, tx));
    }

    @Test
    void endsWith_shouldReturnFalse_whenNoMatch() {
        var evaluator = new EndsWithEvaluator();
        var cond = new Condition(ConditionType.ENDS_WITH, "currency", "AR", null);
        assertFalse(evaluator.evaluate(cond, tx));
    }

    @Test
    void endsWith_shouldReturnFalse_whenNullField() {
        var evaluator = new EndsWithEvaluator();
        var txNull = TransactionDto.builder().build();
        var cond = new Condition(ConditionType.ENDS_WITH, "currency", "SD", null);
        assertFalse(evaluator.evaluate(cond, txNull));
    }

    @Test
    void contains_shouldReturnTrue_whenMatches() {
        var evaluator = new ContainsEvaluator();
        assertThat(evaluator.getSupportedType()).isEqualTo(ConditionType.CONTAINS);
        var cond = new Condition(ConditionType.CONTAINS, "merchantName", "crypto", null);
        assertTrue(evaluator.evaluate(cond, tx));
    }

    @Test
    void contains_shouldReturnTrue_caseInsensitive() {
        var evaluator = new ContainsEvaluator();
        var cond = new Condition(ConditionType.CONTAINS, "merchantName", "CRYPTO", null);
        assertTrue(evaluator.evaluate(cond, tx));
    }

    @Test
    void contains_shouldReturnFalse_whenNoMatch() {
        var evaluator = new ContainsEvaluator();
        var cond = new Condition(ConditionType.CONTAINS, "merchantName", "gambling", null);
        assertFalse(evaluator.evaluate(cond, tx));
    }

    @Test
    void contains_shouldReturnFalse_whenNullField() {
        var evaluator = new ContainsEvaluator();
        var txNull = TransactionDto.builder().build();
        var cond = new Condition(ConditionType.CONTAINS, "merchantName", "test", null);
        assertFalse(evaluator.evaluate(cond, txNull));
    }
}
