package com.chutneytesting.environment.infra;

import static java.util.Optional.ofNullable;

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

        if (targetNode.hasNonNull("security")) {
            JsonNode security = targetNode.get("security");
            if (security.hasNonNull("trustStore")) {
                properties.put("trustStore", security.get("trustStore").textValue());
            }
            if (security.hasNonNull("trustStorePassword")) {
                properties.put("trustStorePassword", security.get("trustStorePassword").textValue());
            }
            if (security.hasNonNull("keyStore")) {
                properties.put("keyStore", security.get("keyStore").textValue());
            }
            if (security.hasNonNull("keyStorePassword")) {
                properties.put("keyStorePassword", security.get("keyStorePassword").textValue());
            }
            if (security.hasNonNull("keyPassword")) {
                properties.put("keyPassword", security.get("keyPassword").textValue());
            }
            if (security.hasNonNull("privateKey")) {
                properties.put("privateKey", security.get("privateKey").textValue());
            }
            if (security.hasNonNull("credential")) {
                JsonNode jsonCredential = security.get("credential");
                if (jsonCredential.hasNonNull("username")) {
                    properties.put("username", jsonCredential.get("username").textValue());
                    properties.put("password", ofNullable(jsonCredential.get("password")).map(JsonNode::textValue).orElse(""));
                }
            }
        }

        return new JsonTarget(name, url, properties);
    }
}
