package org.project.fraudruleapi.rules.model;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleDto implements Serializable {
    private String ruleId;
    private JsonNode data;
    private Long version;
    private Boolean active;
    private Instant createAt;
    private Instant updateAt;

}
