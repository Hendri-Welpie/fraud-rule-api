package org.project.fraudruleapi.rule;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.fraudruleapi.rules.entity.RuleEntity;
import org.project.fraudruleapi.rules.mapper.RuleMapper;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.rules.repository.RuleRepository;
import org.project.fraudruleapi.rules.service.RuleService;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import org.project.fraudruleapi.shared.util.JsonSchemaValidator;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;

    @Mock
    private RuleCache ruleCache;

    @InjectMocks
    private RuleService ruleService;

    private RuleEntity activeRule;
    private RuleEntity inactiveRule;

    @BeforeEach
    void setUp() {
        activeRule = RuleEntity.builder()
                .ruleId("active-rule")
                .active(true)
                .version(1L)
                .data(mock(JsonNode.class))
                .build();

        inactiveRule = RuleEntity.builder()
                .ruleId("inactive-rule")
                .active(false)
                .version(1L)
                .data(mock(JsonNode.class))
                .build();
    }

    @Test
    void createRule_shouldCreateAndEvictCache() {
        JsonNode ruleJson = mock(JsonNode.class);
        when(ruleJson.has("ruleId")).thenReturn(true);
        when(ruleJson.get("ruleId")).thenReturn(ruleJson);
        when(ruleJson.asText()).thenReturn("new-rule");

        when(ruleRepository.findActiveIsTrueForUpdate()).thenReturn(Optional.of(activeRule));
        when(ruleRepository.save(any(RuleEntity.class))).thenReturn(inactiveRule);
        when(ruleCache.getActiveRule()).thenReturn(Mono.just(RuleMapper.INSTANCE.mapToRuleDto(activeRule)));

        StepVerifier.create(ruleService.createRule(ruleJson))
                .verifyComplete();

        verify(jsonSchemaValidator, times(1)).validate(ruleJson);
        verify(ruleRepository, times(2)).save(any(RuleEntity.class));
        verify(ruleCache, times(1)).evictAll();
        verify(ruleCache, times(1)).getActiveRule();
    }

    @Test
    void updateRule_shouldThrowIfVersionMismatch() {
        RuleDto dto = RuleDto.builder()
                .ruleId("inactive-rule")
                .data(mock(JsonNode.class))
                .active(true)
                .version(2L)
                .build();

        when(ruleRepository.findByRuleId("active-rule")).thenReturn(Optional.of(activeRule));

        StepVerifier.create(ruleService.updateRule("active-rule", dto))
                .expectError(OptimisticLockingFailureException.class)
                .verify();
    }

    @Test
    void updateRule_shouldUpdateActiveRule() {
        RuleDto dto = RuleDto.builder()
                .ruleId("inactive-rule")
                .data(mock(JsonNode.class))
                .active(true)
                .version(1L)
                .build();

        when(ruleRepository.findByRuleId("inactive-rule")).thenReturn(Optional.of(inactiveRule));
        when(ruleRepository.findActiveIsTrueForUpdate()).thenReturn(Optional.of(activeRule));
        when(ruleRepository.save(any(RuleEntity.class))).thenReturn(inactiveRule);
        when(ruleCache.getActiveRule()).thenReturn(Mono.just(RuleMapper.INSTANCE.mapToRuleDto(activeRule)));

        StepVerifier.create(ruleService.updateRule("inactive-rule", dto))
                .verifyComplete();

        verify(ruleRepository, times(2)).save(any(RuleEntity.class));
        verify(ruleCache, times(1)).evictAll();
        verify(ruleCache, times(1)).getActiveRule();
    }

    @Test
    void deleteRule_shouldCallRepositoryAndEvictCache() {
        when(ruleRepository.deleteByRuleIdAndActiveIsFalse("inactive-rule")).thenReturn(null);
        StepVerifier.create(ruleService.deleteRule("inactive-rule"))
                .verifyComplete();

        verify(ruleRepository, times(1)).deleteByRuleIdAndActiveIsFalse("inactive-rule");
        verify(ruleCache, times(1)).evictAll();
    }

    @Test
    void findRule_shouldReturnRuleDto() {
        when(ruleRepository.findByRuleId("active-rule")).thenReturn(Optional.of(activeRule));

        StepVerifier.create(ruleService.findRule("active-rule"))
                .expectNextMatches(dto -> dto.getRuleId().equals("active-rule"))
                .verifyComplete();
    }

    @Test
    void findRule_shouldErrorWhenNotFound() {
        when(ruleRepository.findByRuleId("unknown")).thenReturn(Optional.empty());

        StepVerifier.create(ruleService.findRule("unknown"))
                .expectError(ResourceNotFound.class)
                .verify();
    }

    @Test
    void findAllRules_shouldReturnList() {
        when(ruleRepository.findAll()).thenReturn(Arrays.asList(activeRule, inactiveRule));

        StepVerifier.create(ruleService.findAllRules())
                .expectNextMatches(list -> list.size() == 2)
                .verifyComplete();
    }

    @Test
    void findActiveRule_shouldReturnActive() {
        when(ruleRepository.findByActiveIsTrue()).thenReturn(Optional.of(activeRule));

        StepVerifier.create(ruleService.findActiveRule())
                .expectNextMatches(dto -> dto.getRuleId().equals("active-rule"))
                .verifyComplete();
    }

    @Test
    void findActiveRule_shouldErrorWhenNone() {
        when(ruleRepository.findByActiveIsTrue()).thenReturn(Optional.empty());

        StepVerifier.create(ruleService.findActiveRule())
                .expectError(ResourceNotFound.class)
                .verify();
    }
}
