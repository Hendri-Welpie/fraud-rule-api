package org.project.fraudruleapi.shared.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.rules.service.RuleService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleCacheWarmer implements CacheWarmer {
    private final RuleService ruleService;

    @Override
    public void warmUp() {
        ruleService.findActiveRule()
                .doOnSuccess(rule -> log.info("Cache warmed with rule: {}", rule.getRuleId()))
                .subscribe();
    }
}
