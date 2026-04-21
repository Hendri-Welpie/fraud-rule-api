package org.project.fraudruleapi.shared.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "fraud", name = "audit_trail")
public class AuditEntity {

    @Id
    private Long id;

    @Column("trace_id")
    private String traceId;

    @Column("principal")
    private String principal;

    @Column("http_method")
    private String httpMethod;

    @Column("uri")
    private String uri;

    @Column("response_status")
    private Integer responseStatus;

    @Column("timestamp")
    private LocalDateTime timestamp;
}

