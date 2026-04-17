package org.project.fraudruleapi.rules.repository;

import org.project.fraudruleapi.rules.entity.RuleEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RuleRepository extends ReactiveCrudRepository<RuleEntity, String> {

    Mono<RuleEntity> findByRuleId(String ruleId);

    Mono<RuleEntity> findByActiveIsTrue();

    @Query("SELECT * FROM fraud.fraud_rules WHERE active = true FOR UPDATE")
    Mono<RuleEntity> findActiveIsTrueForUpdate();

    Mono<Void> deleteByRuleIdAndActiveIsFalse(String ruleId);
}
