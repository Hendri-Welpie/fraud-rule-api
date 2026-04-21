package org.project.fraudruleapi.shared.audit;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEntityTest {

    @Test
    void shouldBuildAuditEntity() {
        LocalDateTime now = LocalDateTime.now();
        AuditEntity entity = AuditEntity.builder()
                .id(1L)
                .traceId("trace-123")
                .principal("admin")
                .httpMethod("GET")
                .uri("/v1/api/fraud/flag-items")
                .responseStatus(200)
                .timestamp(now)
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getTraceId()).isEqualTo("trace-123");
        assertThat(entity.getPrincipal()).isEqualTo("admin");
        assertThat(entity.getHttpMethod()).isEqualTo("GET");
        assertThat(entity.getUri()).isEqualTo("/v1/api/fraud/flag-items");
        assertThat(entity.getResponseStatus()).isEqualTo(200);
        assertThat(entity.getTimestamp()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetFields() {
        AuditEntity entity = new AuditEntity();
        entity.setId(2L);
        entity.setTraceId("trace-456");
        entity.setPrincipal("user");
        entity.setHttpMethod("POST");
        entity.setUri("/v1/api/fraud/transactions");
        entity.setResponseStatus(201);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getTraceId()).isEqualTo("trace-456");
        assertThat(entity.getPrincipal()).isEqualTo("user");
        assertThat(entity.getHttpMethod()).isEqualTo("POST");
        assertThat(entity.getResponseStatus()).isEqualTo(201);
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        AuditEntity e1 = AuditEntity.builder().id(1L).traceId("t").build();
        AuditEntity e2 = AuditEntity.builder().id(1L).traceId("t").build();
        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }

    @Test
    void shouldSupportToString() {
        AuditEntity entity = AuditEntity.builder().id(1L).principal("admin").build();
        assertThat(entity.toString()).contains("admin");
    }
}

