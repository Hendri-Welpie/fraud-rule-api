package org.project.fraudruleapi.fraud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.fraudruleapi.fraud.model.EvaluationResult;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.fraud.repository.TransactionRepository;
import org.project.fraudruleapi.fraud.service.VelocityCheckService;
import org.project.fraudruleapi.shared.config.ApplicationConfiguration;
import org.project.fraudruleapi.shared.enums.TransactionType;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VelocityCheckServiceTest {

    @Mock
    private ApplicationConfiguration config;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    @Mock
    private ReactiveValueOperations<String, String> valueOps;
    @Mock
    private ApplicationConfiguration.FraudConfiguration fraudConfig;
    @Mock
    private ApplicationConfiguration.VelocityConfig velocityConfig;

    private VelocityCheckService velocityCheckService;

    private final TransactionDto tx = TransactionDto.builder()
            .transactionId("TX1")
            .accountId(1001L)
            .transactionType(TransactionType.TRANSFER)
            .build();

    @BeforeEach
    void setUp() {
        velocityCheckService = new VelocityCheckService(config, transactionRepository, reactiveRedisTemplate);
        when(config.getFraud()).thenReturn(fraudConfig);
        when(fraudConfig.getVelocity()).thenReturn(velocityConfig);
        when(velocityConfig.getWindowSeconds()).thenReturn(60);
        when(velocityConfig.getMaxTransactions()).thenReturn(10);
    }

    @Test
    void checkVelocity_shouldReturnEmpty_whenUnderLimit() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.just(5L));

        StepVerifier.create(velocityCheckService.checkVelocity(tx))
                .assertNext(results -> assertThat(results).isEmpty())
                .verifyComplete();
    }

    @Test
    void checkVelocity_shouldReturnResult_whenOverLimit() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.just(15L));

        StepVerifier.create(velocityCheckService.checkVelocity(tx))
                .assertNext(results -> {
                    assertThat(results).hasSize(1);
                    assertThat(results.get(0).ruleId()).isEqualTo("VELOCITY_CHECK");
                    assertThat(results.get(0).matched()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void checkVelocity_shouldSetExpiry_whenFirstIncrement() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(reactiveRedisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        StepVerifier.create(velocityCheckService.checkVelocity(tx))
                .assertNext(results -> assertThat(results).isEmpty())
                .verifyComplete();
    }

    @Test
    void checkVelocity_shouldFallbackToDb_whenRedisError() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.error(new RuntimeException("Redis down")));
        when(transactionRepository.countByAccountIdAndTimeStampAfter(anyLong(), any()))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(velocityCheckService.checkVelocity(tx))
                .assertNext(results -> assertThat(results).isEmpty())
                .verifyComplete();
    }

    @Test
    void checkVelocity_dbFallback_shouldReturnResult_whenOverLimit() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.error(new RuntimeException("Redis down")));
        when(transactionRepository.countByAccountIdAndTimeStampAfter(anyLong(), any()))
                .thenReturn(Mono.just(15L));

        StepVerifier.create(velocityCheckService.checkVelocity(tx))
                .assertNext(results -> {
                    assertThat(results).hasSize(1);
                    assertThat(results.get(0).ruleId()).isEqualTo("VELOCITY_CHECK");
                })
                .verifyComplete();
    }
}

