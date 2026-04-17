package org.project.fraudruleapi.rules.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fraud_rules", schema = "fraud")
public class RuleEntity {

    @Id
    @Column("rule_id")
    private String ruleId;

    @Column("data")
    private JsonNode data;

    @Column("version")
    private Long version;

    @Column("active")
    private Boolean active;

    @CreatedDate
    @Column("create_at")
    private Instant createAt;

    @LastModifiedDate
    @Column("update_at")
    private Instant updateAt;
}
