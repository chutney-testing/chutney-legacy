package com.chutneytesting.environment.infra;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TargetJsonDeserializer extends JsonDeserializer<JsonTarget> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonTarget deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode targetNode = jsonParser.getCodec().readTree(jsonParser);

        String name = null;
        if (targetNode.hasNonNull("name")) {
            name = targetNode.get("name").textValue();
        }
        String url = null;
        if (targetNode.hasNonNull("url")) {
            url = targetNode.get("url").textValue();
        }
        Map<String, String> properties = new HashMap<>();
        if (targetNode.hasNonNull("properties")) {
            properties = mapper.readValue(targetNode.get("properties").toString(), new TypeReference<>() {
            });
        }

        return new JsonTarget(name, url, properties);
    }
}
