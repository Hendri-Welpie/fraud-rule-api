package org.project.fraudruleapi.shared.converter;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import io.r2dbc.postgresql.codec.Json;

@WritingConverter
public class JsonNodeToJsonConverter implements Converter<JsonNode, Json> {

    @Override
    public Json convert(JsonNode source) {
        return Json.of(source.toString());
    }
}