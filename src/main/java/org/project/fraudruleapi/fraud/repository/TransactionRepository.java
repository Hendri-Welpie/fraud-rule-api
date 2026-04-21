package org.project.fraudruleapi.fraud.repository;

import org.project.fraudruleapi.fraud.entity.TransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, Long>, TransactionRepositoryCustom {

    Mono<Boolean> existsByTransactionId(String transactionId);
}