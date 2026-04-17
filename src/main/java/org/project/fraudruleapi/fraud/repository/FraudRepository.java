package org.project.fraudruleapi.fraud.repository;

import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface FraudRepository extends ReactiveCrudRepository<FraudEntity, Long> {

    Flux<FraudEntity> findByAccountId(Long accountId);

    Flux<FraudEntity> findBySeverity(String severity);
}