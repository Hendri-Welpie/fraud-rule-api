package org.project.fraudruleapi.fraud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule implements Serializable {
    private long id;
    private String name;
    private String description;
    private String type;
    private List<Map<String, Object>> levels;
}