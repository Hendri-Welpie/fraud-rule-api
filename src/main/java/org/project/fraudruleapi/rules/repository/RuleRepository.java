package org.project.fraudruleapi.rules.repository;

import jakarta.persistence.LockModeType;
import org.project.fraudruleapi.rules.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, UUID> {

    Optional<RuleEntity> findByRuleId(String ruleId);

    Optional<RuleEntity> findByActiveIsTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RuleEntity r WHERE r.active = true")
    Optional<RuleEntity> findActiveIsTrueForUpdate();

    RuleEntity deleteByRuleIdAndActiveIsFalse(String ruleId);
}


