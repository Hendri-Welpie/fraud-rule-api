package org.project.fraudruleapi.fraud;

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
import org.project.fraudruleapi.shared.enums.ConditionType;
import org.project.fraudruleapi.shared.enums.TransactionType;
import org.project.fraudruleapi.shared.exception.ConversionException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FraudEvaluatorAdditionalTest {

    private FraudEvaluator fraudEvaluator;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        List<ConditionEvaluator> evaluators = List.of(
                new EqualsEvaluator(),
                new GreaterThanEvaluator(), new GreaterThanOrEqualEvaluator(),
                new LessThanEvaluator(), new LessThanOrEqualEvaluator(),
                new IncludeEvaluator(), new NotEqualsEvaluator()
        );
        ConditionEvaluatorFactory factory = new ConditionEvaluatorFactory(evaluators);
        factory.init();

        ApplicationConfiguration config = new ApplicationConfiguration();
        ApplicationConfiguration.FraudConfiguration fraudConfig = new ApplicationConfiguration.FraudConfiguration();
        ApplicationConfiguration.EvaluationConfig evalConfig = new ApplicationConfiguration.EvaluationConfig();
        evalConfig.setParallelThreshold(5);
        evalConfig.setTimeoutMs(5000L);
        fraudConfig.setEvaluation(evalConfig);

        ApplicationConfiguration.RiskConfig riskConfig = new ApplicationConfiguration.RiskConfig();
        riskConfig.setHighThreshold(60);
        riskConfig.setMediumThreshold(30);
        riskConfig.setLowThreshold(10);
        fraudConfig.setRisk(riskConfig);

        config.setFraud(fraudConfig);
        fraudEvaluator = new FraudEvaluator(factory, config);
    }

    @Test
    void calculateRiskScore_shouldReturnZero_whenNull() {
        assertThat(fraudEvaluator.calculateRiskScore(null)).isEqualTo(0);
    }

    @Test
    void calculateRiskScore_shouldReturnZero_whenEmpty() {
        assertThat(fraudEvaluator.calculateRiskScore(List.of())).isEqualTo(0);
    }

    @Test
    void calculateRiskScore_shouldReturn25_forOneRule() {
        var results = List.of(EvaluationResult.builder().ruleId("r1").matched(true).weight(25).build());
        assertThat(fraudEvaluator.calculateRiskScore(results)).isEqualTo(25);
    }

    @Test
    void calculateRiskScore_shouldCapAt100() {
        var results = List.of(
                EvaluationResult.builder().ruleId("r1").matched(true).weight(25).build(),
                EvaluationResult.builder().ruleId("r2").matched(true).weight(25).build(),
                EvaluationResult.builder().ruleId("r3").matched(true).weight(25).build(),
                EvaluationResult.builder().ruleId("r4").matched(true).weight(25).build(),
                EvaluationResult.builder().ruleId("r5").matched(true).weight(25).build()
        );
        assertThat(fraudEvaluator.calculateRiskScore(results)).isEqualTo(100);
    }

    @Test
    void determineSeverity_shouldReturnCritical() {
        assertThat(fraudEvaluator.determineSeverity(60)).isEqualTo("CRITICAL");
        assertThat(fraudEvaluator.determineSeverity(100)).isEqualTo("CRITICAL");
    }

    @Test
    void determineSeverity_shouldReturnHigh() {
        assertThat(fraudEvaluator.determineSeverity(30)).isEqualTo("HIGH");
        assertThat(fraudEvaluator.determineSeverity(59)).isEqualTo("HIGH");
    }

    @Test
    void determineSeverity_shouldReturnMedium() {
        assertThat(fraudEvaluator.determineSeverity(10)).isEqualTo("MEDIUM");
        assertThat(fraudEvaluator.determineSeverity(29)).isEqualTo("MEDIUM");
    }

    @Test
    void determineSeverity_shouldReturnLow() {
        assertThat(fraudEvaluator.determineSeverity(0)).isEqualTo("LOW");
        assertThat(fraudEvaluator.determineSeverity(9)).isEqualTo("LOW");
    }

    @Test
    void evaluateCondition_shouldReturnFalse_whenNull() {
        TransactionDto tx = TransactionDto.builder().build();
        assertFalse(fraudEvaluator.evaluateCondition(null, tx));
    }

    @Test
    void getRules_shouldThrowConversionException_forInvalidJson() throws Exception {
        String json = "{\"rules\": \"not-an-array\"}";
        RuleDto ruleDto = RuleDto.builder().data(objectMapper.readTree(json)).build();
        assertThrows(ConversionException.class, () -> fraudEvaluator.getRules(ruleDto));
    }

    @Test
    void evaluateAllRules_sequential_shouldReturnMatches() {
        TransactionDto tx = TransactionDto.builder()
                .transferAmount(1000.0)
                .currency("USD")
                .transactionType(TransactionType.TRANSFER)
                .build();

        List<RuleDefinition> rules = List.of(
                RuleDefinition.builder().id("r1").name("Rule1").description("desc1")
                        .condition(new Condition(ConditionType.EQUALS, "currency", "USD", null)).build(),
                RuleDefinition.builder().id("r2").name("Rule2").description("desc2")
                        .condition(new Condition(ConditionType.EQUALS, "currency", "ZAR", null)).build()
        );

        List<EvaluationResult> results = fraudEvaluator.evaluateAllRules(rules, tx);
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().ruleId()).isEqualTo("r1");
    }

    @Test
    void evaluateOr_withNullOperands_shouldReturnFalse() {
        TransactionDto tx = TransactionDto.builder().build();
        Condition orCond = new Condition(ConditionType.OR, null, null, null);
        assertFalse(fraudEvaluator.evaluateCondition(orCond, tx));
    }

    @Test
    void evaluateAnd_withNullOperands_shouldReturnTrue() {
        TransactionDto tx = TransactionDto.builder().build();
        Condition andCond = new Condition(ConditionType.AND, null, null, null);
        assertTrue(fraudEvaluator.evaluateCondition(andCond, tx));
    }

    @Test
    void evaluateNot_withNullOperands_shouldReturnTrue() {
        TransactionDto tx = TransactionDto.builder().build();
        Condition notCond = new Condition(ConditionType.NOT, null, null, null);
        assertTrue(fraudEvaluator.evaluateCondition(notCond, tx));
    }
}

