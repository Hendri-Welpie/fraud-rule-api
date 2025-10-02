package org.project.fraudruleapi.shared.cache;

import lombok.RequiredArgsConstructor;
import org.project.fraudruleapi.rules.mapper.RuleMapper;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.rules.repository.RuleRepository;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class RuleCache {
    private final RuleRepository ruleRepository;

    @Cacheable(value = "rules", key = "'active'")
    public Mono<RuleDto> getActiveRule() {
        return Mono.fromCallable(() -> ruleRepository.findByActiveIsTrue()
                        .orElseThrow(() -> new ResourceNotFound("No active rule found")))
                .map(RuleMapper.INSTANCE::mapToRuleDto)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @CacheEvict(value = "rules", allEntries = true)
    public void evictAll() {
    }
}
