package org.project.fraudruleapi.rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.fraudruleapi.rules.entity.RuleEntity;
import org.project.fraudruleapi.rules.repository.RuleRepository;
import org.project.fraudruleapi.rules.service.RuleService;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleCacheTest {

    @Mock
    private RuleService ruleService;

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleCache rulesCache;

    @Test
    void getRules_shouldReturnRules_whenFound() {
        RuleEntity ruleEntity = RuleEntity.builder()
                .ruleId("rule-123")
                .active(true)
                .build();

        when(ruleRepository.findByActiveIsTrue()).thenReturn(Optional.of(ruleEntity));

        StepVerifier.create(rulesCache.getActiveRule())
                .expectNextMatches(ruleDto -> ruleDto.getRuleId().equals("rule-123"))
                .verifyComplete();

        verify(ruleRepository, times(1)).findByActiveIsTrue();
    }

    @Test
    void getRules_shouldError_whenNotFound() {
        when(ruleRepository.findByActiveIsTrue()).thenReturn(Optional.empty());

        StepVerifier.create(rulesCache.getActiveRule())
                .expectError(ResourceNotFound.class)
                .verify();

        verify(ruleRepository, times(1)).findByActiveIsTrue();
    }

    @Test
    void evictRules_shouldClearCache() {
        rulesCache.evictAll();
    }
}
