package org.project.fraudruleapi.shared.scheduler;

import lombok.RequiredArgsConstructor;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RuleScheduler {
    private final RuleCache ruleCache;

    @Scheduled(cron = "${app.schedule.updateRules}")
    public Mono<Void> updateCachedRules() {
        MDC.put("traceId", UUID.randomUUID().toString());

        return Mono.fromRunnable(ruleCache::evictAll)
                .then(ruleCache.getActiveRule())
                .then();
    }
}