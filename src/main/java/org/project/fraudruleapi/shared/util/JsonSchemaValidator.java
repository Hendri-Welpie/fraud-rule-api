package org.project.fraudruleapi.shared.util;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class JsonSchemaValidator {
    private final ResourceLoader resourceLoader;

    private Schema schema;

    @PostConstruct
    public void init() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:rule-schema.json");
        try (InputStream is = resource.getInputStream()) {
            JSONObject schemaJson = new JSONObject(new JSONTokener(is));
            this.schema = SchemaLoader.load(schemaJson);
        }
    }

    public void validate(final JsonNode jsonNode) {
        JSONObject jsonSchema = new JSONObject(new JSONTokener(jsonNode.toString()));
        schema.validate(jsonSchema);
    }
}
