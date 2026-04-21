package org.project.fraudruleapi.fraud.repository;

import reactor.core.publisher.Mono;

import java.time.Instant;

public interface TransactionRepositoryCustom {
    Mono<Long> countByAccountIdAndTimeStampAfter(Long accountId, Instant startTime);
}

