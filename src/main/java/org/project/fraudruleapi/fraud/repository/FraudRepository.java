package org.project.fraudruleapi.fraud.repository;

import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudRepository extends JpaRepository<FraudEntity, Long> {
}