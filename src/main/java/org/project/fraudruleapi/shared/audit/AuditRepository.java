package org.project.fraudruleapi.shared.audit;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AuditRepository extends ReactiveCrudRepository<AuditEntity, Long> {

    Flux<AuditEntity> findByPrincipal(String principal);

    Flux<AuditEntity> findByUri(String uri);
}

