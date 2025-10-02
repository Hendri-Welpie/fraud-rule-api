package org.project.fraudruleapi.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.fraudruleapi.shared.util.JsonSchemaValidator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonSchemaValidatorTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    private JsonSchemaValidator validator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // Mock resource to return a simple schema JSON
        String schemaJson = """
                {
                    "type": "object",
                    "properties": {
                        "ruleId": { "type": "string" },
                        "active": { "type": "boolean" }
                    },
                    "required": ["ruleId", "active"]
                }
                """;

        InputStream is = new ByteArrayInputStream(schemaJson.getBytes());
        when(resourceLoader.getResource("classpath:rule-schema.json")).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(is);

        validator = new JsonSchemaValidator(resourceLoader);
        validator.init();
    }

    @Test
    void validate_shouldPassForValidJson() throws Exception {
        JsonNode validJson = objectMapper.readTree("""
                {
                    "ruleId": "123",
                    "active": true
                }
                """);

        assertDoesNotThrow(() -> validator.validate(validJson));
    }

    @Test
    void validate_shouldThrowExceptionForInvalidJson() throws Exception {
        JsonNode invalidJson = objectMapper.readTree("""
                {
                    "ruleId": 123,
                    "active": "yes"
                }
                """);

        assertThrows(org.everit.json.schema.ValidationException.class,
                () -> validator.validate(invalidJson));
    }

    @Test
    void init_shouldLoadSchemaFromResource() throws IOException {
        // Verify that getResource and getInputStream were called
        verify(resourceLoader, times(1)).getResource("classpath:rule-schema.json");
        verify(resource, times(1)).getInputStream();
        assertNotNull(validator); // schema is loaded in @PostConstruct
    }
}
