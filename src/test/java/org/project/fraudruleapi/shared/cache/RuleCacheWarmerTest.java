package org.project.fraudruleapi.shared.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.rules.service.RuleService;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleCacheWarmerTest {

    @Mock
    private RuleService ruleService;

    @InjectMocks
    private RuleCacheWarmer ruleCacheWarmer;

    @Test
    void warmUp_shouldCallFindActiveRule() {
        RuleDto ruleDto = new RuleDto();
        ruleDto.setRuleId("active-rule");
        when(ruleService.findActiveRule()).thenReturn(Mono.just(ruleDto));

        ruleCacheWarmer.warmUp();

        verify(ruleService, times(1)).findActiveRule();
    }
}
