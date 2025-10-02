package org.project.fraudruleapi.rule;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.fraudruleapi.rules.controller.RuleController;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.rules.service.RuleService;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RuleControllerTest {

    @Mock
    private RuleService ruleService;

    @InjectMocks
    private RuleController ruleController;

    private JsonNode mockJson;

    private RuleDto ruleDto1;
    private RuleDto ruleDto2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create dummy JSON and RuleDto objects
        mockJson = mock(JsonNode.class);

        ruleDto1 = RuleDto.builder()
                .ruleId("rule1")
                .active(true)
                .build();

        ruleDto2 = RuleDto.builder()
                .ruleId("rule2")
                .active(false)
                .build();
    }

    @Test
    void create_shouldReturnCreatedResponse() {
        when(ruleService.createRule(any(JsonNode.class))).thenReturn(Mono.empty());

        StepVerifier.create(ruleController.create(mockJson))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(Objects.requireNonNull(response.getHeaders().getLocation()).toString()).isEqualTo("/rules");
                })
                .verifyComplete();

        verify(ruleService, times(1)).createRule(mockJson);
    }

    @Test
    void getRule_shouldReturnRuleDto() {
        when(ruleService.findRule("rule1")).thenReturn(Mono.just(ruleDto1));

        StepVerifier.create(ruleController.getRule("rule1"))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isEqualTo(ruleDto1);
                })
                .verifyComplete();

        verify(ruleService, times(1)).findRule("rule1");
    }

    @Test
    void getAllRules_shouldReturnListOfRules() {
        when(ruleService.findAllRules()).thenReturn(Mono.just(List.of(ruleDto1, ruleDto2)));

        StepVerifier.create(ruleController.getAllRules())
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).containsExactly(ruleDto1, ruleDto2);
                })
                .verifyComplete();

        verify(ruleService, times(1)).findAllRules();
    }

    @Test
    void updateRule_shouldReturnOk() {
        when(ruleService.updateRule(eq("rule1"), any(RuleDto.class))).thenReturn(Mono.empty());

        StepVerifier.create(ruleController.updateRule("rule1", ruleDto1))
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(ruleService, times(1)).updateRule("rule1", ruleDto1);
    }

    @Test
    void deleteRule_shouldReturnNoContent() {
        when(ruleService.deleteRule("rule1")).thenReturn(Mono.empty());

        StepVerifier.create(ruleController.delete("rule1"))
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();

        verify(ruleService, times(1)).deleteRule("rule1");
    }
}
