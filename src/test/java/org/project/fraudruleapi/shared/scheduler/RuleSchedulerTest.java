package org.project.fraudruleapi.shared.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.project.fraudruleapi.rules.model.RuleDto;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleSchedulerTest {

    @Mock
    private RuleCache ruleCache;

    @InjectMocks
    private RuleScheduler ruleScheduler;

    @Test
    void updateCachedRules_shouldEvictAndReload() {
        when(ruleCache.getActiveRule()).thenReturn(Mono.just(new RuleDto()));

        StepVerifier.create(ruleScheduler.updateCachedRules())
                .verifyComplete();

        verify(ruleCache, times(1)).evictAll();
        verify(ruleCache, times(1)).getActiveRule();
    }
}
