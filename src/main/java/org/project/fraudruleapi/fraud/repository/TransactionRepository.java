package org.project.fraudruleapi.fraud.repository;

import org.project.fraudruleapi.fraud.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    Long countByUserIdAndTimeStampGreaterThanEqual(Long userId,
                                                   Instant timeStamp);
}