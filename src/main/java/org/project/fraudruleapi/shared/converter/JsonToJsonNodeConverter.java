package org.project.fraudruleapi.shared.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class JsonToJsonNodeConverter implements Converter<Json, JsonNode> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode convert(Json source) {
        try {
            return objectMapper.readTree(source.asString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Json to JsonNode", e);
        }
    }
}