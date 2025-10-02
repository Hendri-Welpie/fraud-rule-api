package org.project.fraudruleapi.fraud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.entity.TransactionEntity;
import org.project.fraudruleapi.fraud.evaluator.FraudEvaluator;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.RuleDefinition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.fraud.repository.FraudRepository;
import org.project.fraudruleapi.fraud.repository.TransactionRepository;
import org.project.fraudruleapi.fraud.service.FraudService;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.project.fraudruleapi.shared.enums.ChannelType;
import org.project.fraudruleapi.shared.enums.ConditionalType;
import org.project.fraudruleapi.shared.enums.StatusType;
import org.project.fraudruleapi.shared.enums.TransactionType;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class FraudServiceTest {

    @Mock
    private RuleCache ruleCache;
    @Mock private FraudEvaluator fraudEvaluator;
    @Mock private FraudRepository fraudRepository;
    @Mock private TransactionRepository transactionRepository;

    private FraudService fraudService;

    @BeforeEach
    void setup() {
        fraudService = new FraudService(ruleCache, fraudEvaluator, fraudRepository, transactionRepository);
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

        Mockito.when(transactionRepository.save(Mockito.any())).thenReturn(txEntity);
        Mockito.when(ruleCache.getActiveRule()).thenReturn(Mono.just(new RuleDto()));
        Mockito.when(fraudEvaluator.getRules(Mockito.any())).thenReturn(List.of(rule));
        Mockito.when(fraudEvaluator.evaluateCondition(Mockito.any(), Mockito.eq(tx))).thenReturn(true);
        Mockito.when(fraudRepository.save(Mockito.any(FraudEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(fraudService.validate(tx))
                .verifyComplete();

        Mockito.verify(transactionRepository).save(Mockito.any());
        Mockito.verify(fraudRepository).save(Mockito.any());
    }

    @Test
    void getFlaggedItem_notFound_error() {
        Mockito.when(fraudRepository.findById(99L)).thenReturn(Optional.empty());

        StepVerifier.create(fraudService.getFlaggedItem(99L))
                .expectError(ResourceNotFound.class)
                .verify();
    }
}
