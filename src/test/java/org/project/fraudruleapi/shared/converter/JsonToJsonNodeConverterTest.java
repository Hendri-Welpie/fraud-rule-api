package org.project.fraudruleapi.shared.converter;

import com.fasterxml.jackson.databind.JsonNode;
import io.r2dbc.postgresql.codec.Json;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonToJsonNodeConverterTest {

    private final JsonToJsonNodeConverter converter = new JsonToJsonNodeConverter();

    @Test
    void convert_shouldConvertJsonToJsonNode() {
        Json json = Json.of("{\"key\":\"value\"}");

        JsonNode result = converter.convert(json);

        assertNotNull(result);
        assertTrue(result.has("key"));
        assertEquals("value", result.get("key").asText());
    }

    @Test
    void convert_shouldThrowExceptionForInvalidJson() {
        Json json = Json.of("invalid json");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> converter.convert(json));
        assertEquals("Failed to convert Json to JsonNode", exception.getMessage());
    }
}
