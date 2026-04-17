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
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;

    @Mock
    private RuleCache ruleCache;

    @Mock
    private R2dbcEntityTemplate r2dbcEntityTemplate;

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

        RuleEntity newRule = RuleEntity.builder()
                .ruleId("new-rule")
                .active(true)
                .data(ruleJson)
                .build();

        when(ruleRepository.findActiveIsTrueForUpdate()).thenReturn(Mono.just(activeRule));
        when(ruleRepository.save(any(RuleEntity.class))).thenReturn(Mono.just(activeRule)); // return the saved entity
        when(r2dbcEntityTemplate.insert(any(RuleEntity.class))).thenReturn(Mono.just(newRule));
        when(ruleCache.getActiveRule()).thenReturn(Mono.just(RuleMapper.INSTANCE.mapToRuleDto(newRule))); // return new active rule

        StepVerifier.create(ruleService.createRule(ruleJson))
                .expectNext("new-rule")
                .verifyComplete();

        verify(jsonSchemaValidator, times(1)).validate(ruleJson);
        verify(ruleRepository, times(1)).save(any(RuleEntity.class)); // only one save for deactivating
        verify(r2dbcEntityTemplate, times(1)).insert(any(RuleEntity.class));
        verify(ruleCache, times(1)).evictAll();
        verify(ruleCache, times(1)).getActiveRule();
    }


    @Test
    void updateRule_shouldUpdateActiveRule() {
        RuleDto dto = RuleDto.builder()
                .ruleId("inactive-rule")
                .data(mock(JsonNode.class))
                .active(true)
                .version(1L)
                .build();

        when(ruleRepository.findByRuleId("inactive-rule")).thenReturn(Mono.just(inactiveRule));
        when(ruleRepository.findActiveIsTrueForUpdate()).thenReturn(Mono.just(activeRule));
        when(ruleRepository.save(any(RuleEntity.class))).thenReturn(Mono.just(inactiveRule));
        when(ruleCache.getActiveRule()).thenReturn(Mono.just(RuleMapper.INSTANCE.mapToRuleDto(activeRule)));

        StepVerifier.create(ruleService.updateRule("inactive-rule", dto))
                .verifyComplete();

        verify(ruleRepository, times(2)).save(any(RuleEntity.class));
        verify(ruleCache, times(1)).evictAll();
        verify(ruleCache, times(1)).getActiveRule();
    }

    @Test
    void deleteRule_shouldCallRepositoryAndEvictCache() {
        when(ruleRepository.deleteByRuleIdAndActiveIsFalse("inactive-rule")).thenReturn(Mono.empty());
        StepVerifier.create(ruleService.deleteRule("inactive-rule"))
                .verifyComplete();

        verify(ruleRepository, times(1)).deleteByRuleIdAndActiveIsFalse("inactive-rule");
        verify(ruleCache, times(1)).evictAll();
    }

    @Test
    void findRule_shouldReturnRuleDto() {
        when(ruleRepository.findByRuleId("active-rule")).thenReturn(Mono.just(activeRule));

        StepVerifier.create(ruleService.findRule("active-rule"))
                .expectNextMatches(dto -> dto.getRuleId().equals("active-rule"))
                .verifyComplete();
    }

    @Test
    void findRule_shouldErrorWhenNotFound() {
        when(ruleRepository.findByRuleId("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(ruleService.findRule("unknown"))
                .expectError(ResourceNotFound.class)
                .verify();
    }

    @Test
    void findAllRules_shouldReturnList() {
        when(ruleRepository.findAll()).thenReturn(Flux.fromIterable(Arrays.asList(activeRule, inactiveRule)));

        StepVerifier.create(ruleService.findAllRules())
                .expectNextMatches(list -> list.size() == 2)
                .verifyComplete();
    }

    @Test
    void findActiveRule_shouldReturnActive() {
        when(ruleRepository.findByActiveIsTrue()).thenReturn(Mono.just(activeRule));

        StepVerifier.create(ruleService.findActiveRule())
                .expectNextMatches(dto -> dto.getRuleId().equals("active-rule"))
                .verifyComplete();
    }

    @Test
    void findActiveRule_shouldErrorWhenNone() {
        when(ruleRepository.findByActiveIsTrue()).thenReturn(Mono.empty());

        StepVerifier.create(ruleService.findActiveRule())
                .expectError(ResourceNotFound.class)
                .verify();
    }
}
