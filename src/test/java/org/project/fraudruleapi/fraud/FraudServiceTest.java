package org.project.fraudruleapi.fraud;

import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.entity.TransactionEntity;
import org.project.fraudruleapi.fraud.evaluator.FraudEvaluator;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.EvaluationResult;
import org.project.fraudruleapi.fraud.model.RuleDefinition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.fraud.repository.FraudRepository;
import org.project.fraudruleapi.fraud.repository.TransactionRepository;
import org.project.fraudruleapi.fraud.service.FraudService;
import org.project.fraudruleapi.fraud.service.VelocityCheckService;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.project.fraudruleapi.shared.config.ApplicationConfiguration;
import org.project.fraudruleapi.shared.enums.ChannelType;
import org.project.fraudruleapi.shared.enums.ConditionalType;
import org.project.fraudruleapi.shared.enums.StatusType;
import org.project.fraudruleapi.shared.enums.TransactionType;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FraudServiceTest {

    @Mock
    private RuleCache ruleCache;
    @Mock
    private FraudEvaluator fraudEvaluator;
    @Mock
    private FraudRepository fraudRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ApplicationConfiguration config;
    @Mock
    private VelocityCheckService velocityCheckService;
    @Mock
    private ApplicationConfiguration.FraudConfiguration fraudConfig;
    @Mock
    private ApplicationConfiguration.VelocityConfig velocityConfig;
    @Mock
    private ApplicationConfiguration.RiskConfig riskConfig;
    @Mock
    private Counter counter;

    private FraudService fraudService;

    @BeforeEach
    void setup() {
        fraudService = new FraudService(ruleCache, fraudEvaluator, fraudRepository,
                transactionRepository, config, velocityCheckService, counter, counter);
    }

    @Test
    void validate_transactionSavedAndFraudDetected_success() {
        TransactionDto tx = new TransactionDto(
                "tx123", 1L, 10L, "USD", 500.0,
                LocalDateTime.now(), TransactionType.TRANSFER,
                ChannelType.MOBILE_APP, "M123", "Amazon",
                200L, "127.0.0.1", "device1", "NYC",
                StatusType.PENDING
        );

        TransactionEntity txEntity = new TransactionEntity();
        txEntity.setTransactionId("tx123");

        RuleDefinition rule = RuleDefinition.builder()
                .id("rule1")
                .name("High Amount")
                .description("Amount > 100")
                .condition(new Condition(ConditionalType.GREATER_THAN, "amount", 100, null))
                .build();

        List<EvaluationResult> results = List.of(
                EvaluationResult.builder()
                        .ruleId("rule1")
                        .ruleName("High Amount")
                        .description("Amount > 100")
                        .matched(true)
                        .evaluationTimeMs(10)
                        .build()
        );

        // Mock config
        when(config.getFraud()).thenReturn(fraudConfig);
        when(fraudConfig.getVelocity()).thenReturn(velocityConfig);
        when(velocityConfig.isEnabled()).thenReturn(false);
        when(fraudConfig.getRisk()).thenReturn(riskConfig);
        when(riskConfig.getHighThreshold()).thenReturn(80);
        when(riskConfig.getMediumThreshold()).thenReturn(50);
        when(riskConfig.getLowThreshold()).thenReturn(20);

        when(transactionRepository.save(any())).thenReturn(Mono.just(txEntity));
        when(ruleCache.getActiveRule()).thenReturn(Mono.just(new RuleDto()));
        when(fraudEvaluator.getRules(any())).thenReturn(List.of(rule));
        when(fraudEvaluator.evaluateAllRules(any(), any())).thenReturn(results);
        when(fraudEvaluator.calculateRiskScore(any())).thenReturn(25);
        when(fraudEvaluator.determineSeverity(25)).thenReturn("MEDIUM");
        when(fraudRepository.saveAll(any(Iterable.class))).thenReturn(Flux.empty());

        StepVerifier.create(fraudService.validate(tx))
                .expectNextMatches(response ->
                        response.isFraud() &&
                                response.riskScore() == 25 &&
                                "MEDIUM".equals(response.severity()))
                .verifyComplete();

        verify(transactionRepository).save(any());
    }

    @Test
    void validate_transactionSavedAndFraudDetected_withVelocity_success() {
        TransactionDto tx = new TransactionDto(
                "tx123", 1L, 10L, "USD", 500.0,
                LocalDateTime.now(), TransactionType.TRANSFER,
                ChannelType.MOBILE_APP, "M123", "Amazon",
                200L, "127.0.0.1", "device1", "NYC",
                StatusType.PENDING
        );

        TransactionEntity txEntity = new TransactionEntity();
        txEntity.setTransactionId("tx123");

        RuleDefinition rule = RuleDefinition.builder()
                .id("rule1")
                .name("High Amount")
                .description("Amount > 100")
                .condition(new Condition(ConditionalType.GREATER_THAN, "amount", 100, null))
                .build();

        List<EvaluationResult> results = List.of(
                EvaluationResult.builder()
                        .ruleId("rule1")
                        .ruleName("High Amount")
                        .description("Amount > 100")
                        .matched(true)
                        .evaluationTimeMs(10)
                        .build()
        );

        // Mock config with velocity enabled
        when(config.getFraud()).thenReturn(fraudConfig);
        when(fraudConfig.getVelocity()).thenReturn(velocityConfig);
        when(velocityConfig.isEnabled()).thenReturn(true);
        when(fraudConfig.getRisk()).thenReturn(riskConfig);
        when(riskConfig.getHighThreshold()).thenReturn(80);
        when(riskConfig.getMediumThreshold()).thenReturn(50);
        when(riskConfig.getLowThreshold()).thenReturn(20);

        when(velocityCheckService.checkVelocity(any(TransactionDto.class))).thenReturn(Mono.just(List.of())); // velocity check passes

        when(transactionRepository.save(any())).thenReturn(Mono.just(txEntity));
        when(ruleCache.getActiveRule()).thenReturn(Mono.just(new RuleDto()));
        when(fraudEvaluator.getRules(any())).thenReturn(List.of(rule));
        when(fraudEvaluator.evaluateAllRules(any(), any())).thenReturn(results);
        when(fraudEvaluator.calculateRiskScore(any())).thenReturn(25);
        when(fraudEvaluator.determineSeverity(25)).thenReturn("MEDIUM");
        when(fraudRepository.saveAll(any(Iterable.class))).thenReturn(Flux.empty());

        StepVerifier.create(fraudService.validate(tx))
                .expectNextMatches(response ->
                        response.isFraud() &&
                                response.riskScore() == 25 &&
                                "MEDIUM".equals(response.severity()))
                .verifyComplete();

        verify(velocityCheckService, times(1)).checkVelocity(tx);
    }

    @Test
    void getFlaggedItem_notFound_error() {
        when(fraudRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(fraudService.getFlaggedItem(99L))
                .expectError(ResourceNotFound.class)
                .verify();
    }

    @Test
    void getFlaggedItems_success() {
        FraudEntity fraud = FraudEntity.builder().id(1L).transactionId("t1").build();
        when(fraudRepository.findAll()).thenReturn(Flux.just(fraud));

        StepVerifier.create(fraudService.getFlaggedItems())
                .expectNext(fraud)
                .verifyComplete();
    }

    @Test
    void getFraudByAccountId_success() {
        FraudEntity fraud = FraudEntity.builder().id(1L).transactionId("t1").accountId(123L).build();
        when(fraudRepository.findByAccountId(123L)).thenReturn(Flux.just(fraud));

        StepVerifier.create(fraudService.getFraudByAccountId(123L))
                .expectNext(fraud)
                .verifyComplete();
    }

    @Test
    void getFraudBySeverity_success() {
        FraudEntity fraud = FraudEntity.builder().id(1L).transactionId("t1").severity("HIGH").build();
        when(fraudRepository.findBySeverity("HIGH")).thenReturn(Flux.just(fraud));

        StepVerifier.create(fraudService.getFraudBySeverity("HIGH"))
                .expectNext(fraud)
                .verifyComplete();
    }
}
