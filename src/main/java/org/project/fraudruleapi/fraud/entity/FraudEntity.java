package org.project.fraudruleapi.fraud.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "fraud", name = "fraud_events")
public class FraudEntity {
    @Id
    @Column("id")
    private Long id;

    @Column("transaction_id")
    private String transactionId;

    @Column("account_id")
    private long accountId;

    @Column("transaction_date")
    private Instant transactionDate;

    @Column("rule_id")
    private String ruleId;

    @Column("type")
    private String type;

    @Column("reason")
    private String reason;

    @Column("severity")
    private String severity;

    @Column("detected_at")
    private Instant detectedAt;
}