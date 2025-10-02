package org.project.fraudruleapi.fraud.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "fraud", name = "fraud_events")
public class FraudEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "transaction_date")
    private Instant transactionDate;

    @Column(name = "rule_id")
    private String ruleId;

    @Column(name = "type")
    private String type;

    @Column(name = "reason")
    private String reason;

    @Column(name = "severity")
    private String severity;

    @Column(name = "detected_at")
    private Instant detectedAt;
}