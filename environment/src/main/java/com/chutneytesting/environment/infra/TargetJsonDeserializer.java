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
import java.util.Optional;

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
        JsonTarget.JsonSecurityInfo securityInfo = null;
        if (targetNode.hasNonNull("security")) {
            JsonNode security = targetNode.get("security");
            String trustStore = null;
            if (security.hasNonNull("trustStore")) {
                trustStore = security.get("trustStore").textValue();
            }
            String trustStorePassword = null;
            if (security.hasNonNull("trustStorePassword")) {
                trustStorePassword = security.get("trustStorePassword").textValue();
            }
            String keyStore = null;
            if (security.hasNonNull("keyStore")) {
                keyStore = security.get("keyStore").textValue();
            }
            String keyStorePassword = null;
            if (security.hasNonNull("keyStorePassword")) {
                keyStorePassword = security.get("keyStorePassword").textValue();
            }
            String privateKey = null;
            if (security.hasNonNull("privateKey")) {
                privateKey = security.get("privateKey").textValue();
            }
            JsonTarget.JsonCredential credential = null;

            if (security.hasNonNull("credential")) {
                JsonNode jsonCredential = security.get("credential");
                if (jsonCredential.hasNonNull("username")) {
                    String username = jsonCredential.get("username").textValue();
                    String password = Optional.ofNullable(jsonCredential.get("password")).map(JsonNode::textValue).orElse("");
                    credential = new JsonTarget.JsonCredential(username, password);
                }
            }
            securityInfo = new JsonTarget.JsonSecurityInfo(credential, trustStore,trustStorePassword, keyStore, keyStorePassword, privateKey );
        }
        return new JsonTarget(url, properties, securityInfo, name);
    }
}
