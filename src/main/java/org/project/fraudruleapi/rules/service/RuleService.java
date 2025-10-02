package org.project.fraudruleapi.rules.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.project.fraudruleapi.rules.entity.RuleEntity;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import org.project.fraudruleapi.rules.mapper.RuleMapper;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.rules.repository.RuleRepository;
import org.project.fraudruleapi.shared.util.JsonSchemaValidator;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final RuleCache ruleCache;

    public Mono<Void> createRule(final JsonNode ruleJson) {
        jsonSchemaValidator.validate(ruleJson);

        return Mono.fromCallable(() -> {
                    ruleRepository.findActiveIsTrueForUpdate().ifPresent(ruleEntity -> {
                        ruleEntity.setActive(false);
                        ruleRepository.save(ruleEntity);
                    });

                    return ruleRepository.save(RuleEntity.builder()
                            .ruleId(ruleJson.has("ruleId")
                                    ? ruleJson.get("ruleId").asText()
                                    : UUID.randomUUID().toString())
                            .data(ruleJson)
                            .active(true)
                            .build());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.fromRunnable(ruleCache::evictAll))
                .then(ruleCache.getActiveRule())
                .then();
    }

    public Mono<Void> updateRule(final String ruleId, final RuleDto ruleDto) {
        return Mono.fromCallable(() -> {
                    RuleEntity ruleEntity = ruleRepository.findByRuleId(ruleId)
                            .orElseThrow(() -> new ResourceNotFound(
                                    String.format("Rule %s does not exist", ruleId)));

                    if (ruleDto.getVersion() != null && !ruleDto.getVersion().equals(ruleEntity.getVersion())) {
                        throw new OptimisticLockingFailureException("Version mismatch");
                    }

                    if (ruleDto.getData() != null) {
                        jsonSchemaValidator.validate(ruleDto.getData());
                        ruleEntity.setData(ruleDto.getData());
                    }

                    if (Boolean.TRUE.equals(ruleDto.getActive()) && !ruleEntity.getActive()) {
                        ruleRepository.findActiveIsTrueForUpdate()
                                .ifPresent(rule -> {
                                    rule.setActive(false);
                                    ruleRepository.save(rule);
                                });
                        ruleEntity.setActive(true);
                    } else {
                        ruleEntity.setActive(false);
                    }
                    return ruleRepository.save(ruleEntity);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.fromRunnable(ruleCache::evictAll))
                .then(Mono.defer(ruleCache::getActiveRule))
                .then();
    }

    public Mono<Void> deleteRule(final String ruleId) {
        return Mono.fromCallable(() -> ruleRepository.deleteByRuleIdAndActiveIsFalse(ruleId))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.fromRunnable(ruleCache::evictAll));
    }

    public Mono<RuleDto> findRule(String ruleId) {
        return Mono.fromCallable(() -> ruleRepository.findByRuleId(ruleId)
                        .orElseThrow(() -> new ResourceNotFound(
                                String.format("Rule %s does not exist", ruleId))))
                .map(RuleMapper.INSTANCE::mapToRuleDto)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<RuleDto>> findAllRules() {
        return Mono.fromCallable(() -> ruleRepository.findAll()
                        .stream()
                        .map(RuleMapper.INSTANCE::mapToRuleDto)
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<RuleDto> findActiveRule() {
        return Mono.fromCallable(() -> ruleRepository.findByActiveIsTrue()
                        .orElseThrow(() -> new ResourceNotFound("No active rule found")))
                .map(RuleMapper.INSTANCE::mapToRuleDto)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
