package org.project.fraudruleapi.rules.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.project.fraudruleapi.shared.converter.JsonNodeConverter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fraud_rules")
public class RuleEntity {

    @Id
    @Column(name = "rule_id", unique = true, nullable = false)
    private String ruleId;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode data;

    @Version
    private Long version;

    @Column(nullable = false)
    private Boolean active;

    @CreatedDate
    @Column(name = "create_at")
    private Instant createAt;

    @LastModifiedDate
    @Column(name = "update_at")
    private Instant updateAt;
}
