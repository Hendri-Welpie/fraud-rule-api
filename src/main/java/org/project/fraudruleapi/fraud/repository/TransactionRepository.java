package org.project.fraudruleapi.fraud.repository;

import org.project.fraudruleapi.fraud.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, Long> {

    @Query("SELECT COUNT(*) FROM fraud.transaction WHERE account_id = :accountId AND timestamp >= :startTime")
    Mono<Long> countByAccountIdAndTimeStampAfter(
            @Param("accountId") Long accountId,
            @Param("startTime") Instant startTime);
}