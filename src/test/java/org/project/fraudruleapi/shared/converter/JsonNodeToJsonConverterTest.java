package org.project.fraudruleapi.shared.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonNodeToJsonConverterTest {

    private final JsonNodeToJsonConverter converter = new JsonNodeToJsonConverter();

    @Test
    void convert_shouldConvertJsonNodeToJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree("{\"key\":\"value\"}");

        Json result = converter.convert(jsonNode);

        assertNotNull(result);
        assertEquals("{\"key\":\"value\"}", result.asString());
    }
}
