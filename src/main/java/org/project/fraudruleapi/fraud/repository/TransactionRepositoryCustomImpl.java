package org.project.fraudruleapi.fraud.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryCustomImpl implements TransactionRepositoryCustom {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Long> countByAccountIdAndTimeStampAfter(Long accountId, Instant startTime) {
        return databaseClient.sql("SELECT COUNT(*) FROM fraud.transaction WHERE account_id = :accountId AND timestamp >= :startTime")
                .bind("accountId", accountId)
                .bind("startTime", startTime)
                .map(row -> row.get(0, Long.class))
                .one()
                .defaultIfEmpty(0L);
    }
}

