package org.project.fraudruleapi.rules.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.project.fraudruleapi.rules.entity.RuleEntity;
import org.project.fraudruleapi.rules.mapper.RuleMapper;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.rules.repository.RuleRepository;
import org.project.fraudruleapi.shared.cache.RuleCache;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import org.project.fraudruleapi.shared.util.JsonSchemaValidator;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final RuleCache ruleCache;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Transactional
    public Mono<String> createRule(final JsonNode ruleJson) {
        jsonSchemaValidator.validate(ruleJson);

        String ruleId = ruleJson.has("ruleId")
                ? ruleJson.get("ruleId").asText()
                : UUID.randomUUID().toString();

        RuleEntity newRule = RuleEntity.builder()
                .ruleId(ruleId)
                .data(ruleJson)
                .active(true)
                .build();

        return ruleRepository.findActiveIsTrueForUpdate()
                .flatMap(activeRule -> {
                    activeRule.setActive(false);
                    return ruleRepository.save(activeRule);
                })
                .then(r2dbcEntityTemplate.insert(newRule))
                .then(Mono.fromRunnable(ruleCache::evictAll))
                .then(ruleCache.getActiveRule())
                .thenReturn(ruleId);
    }

    @Transactional
    public Mono<Void> updateRule(final String ruleId, final RuleDto ruleDto) {
        return ruleRepository.findByRuleId(ruleId)
                .switchIfEmpty(Mono.error(new ResourceNotFound(String.format("Rule %s does not exist", ruleId))))
                .flatMap(ruleEntity -> {
                    if (ruleDto.getVersion() != null && !ruleDto.getVersion().equals(ruleEntity.getVersion())) {
                        return Mono.error(new RuntimeException("Version mismatch"));
                    }

                    if (ruleDto.getData() != null) {
                        jsonSchemaValidator.validate(ruleDto.getData());
                        ruleEntity.setData(ruleDto.getData());
                    }

                    if (Boolean.TRUE.equals(ruleDto.getActive()) && !ruleEntity.getActive()) {
                        return ruleRepository.findActiveIsTrueForUpdate()
                                .flatMap(activeRule -> {
                                    activeRule.setActive(false);
                                    return ruleRepository.save(activeRule);
                                })
                                .then(Mono.fromCallable(() -> {
                                    ruleEntity.setActive(true);
                                    return ruleEntity;
                                }));
                    } else {
                        ruleEntity.setActive(ruleDto.getActive() != null ? ruleDto.getActive() : ruleEntity.getActive());
                        return Mono.just(ruleEntity);
                    }
                })
                .flatMap(ruleRepository::save)
                .then(Mono.fromRunnable(ruleCache::evictAll))
                .then(ruleCache.getActiveRule())
                .then();
    }

    @Transactional
    public Mono<Void> deleteRule(final String ruleId) {
        return ruleRepository.deleteByRuleIdAndActiveIsFalse(ruleId)
                .then(Mono.fromRunnable(ruleCache::evictAll));
    }

    public Mono<RuleDto> findRule(String ruleId) {
        return ruleRepository.findByRuleId(ruleId)
                .switchIfEmpty(Mono.error(new ResourceNotFound(String.format("Rule %s does not exist", ruleId))))
                .map(RuleMapper.INSTANCE::mapToRuleDto);
    }

    public Mono<List<RuleDto>> findAllRules() {
        return ruleRepository.findAll()
                .map(RuleMapper.INSTANCE::mapToRuleDto)
                .collectList();
    }

    public Mono<RuleDto> findActiveRule() {
        return ruleRepository.findByActiveIsTrue()
                .switchIfEmpty(Mono.error(new ResourceNotFound("No active rule found")))
                .map(RuleMapper.INSTANCE::mapToRuleDto);
    }
}
