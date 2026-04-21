package org.project.fraudruleapi.fraud.repository;

import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface FraudRepository extends ReactiveCrudRepository<FraudEntity, Long> {

    Flux<FraudEntity> findByAccountId(Long accountId);

    Flux<FraudEntity> findBySeverity(String severity);

    @Query("SELECT * FROM fraud.fraud_events ORDER BY detected_at DESC LIMIT :limit OFFSET :offset")
    Flux<FraudEntity> findAllPaginated(int limit, long offset);

    @Query("SELECT * FROM fraud.fraud_events WHERE severity = :severity ORDER BY detected_at DESC LIMIT :limit OFFSET :offset")
    Flux<FraudEntity> findBySeverityPaginated(String severity, int limit, long offset);
}