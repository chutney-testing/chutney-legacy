package com.chutneytesting.design.infra.storage.environment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chutneytesting.design.infra.storage.environment.JsonTarget.JsonCredential;
import com.chutneytesting.design.infra.storage.environment.JsonTarget.JsonSecurityInfo;
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
           properties = mapper.readValue(targetNode.get("properties").toString(), new TypeReference<Map<String, ? extends String>>() {});
        }
        JsonSecurityInfo securityInfo = null;
        if (targetNode.hasNonNull("security")) {
            JsonNode secu = targetNode.get("security");
            String trustStore = null;
            if (secu.hasNonNull("trustStore")) {
                trustStore = secu.get("trustStore").textValue();
            }
            String trustStorePassword = null;
            if (secu.hasNonNull("trustStorePassword")) {
                trustStorePassword = secu.get("trustStorePassword").textValue();
            }
            String keyStore = null;
            if (secu.hasNonNull("keyStore")) {
                keyStore = secu.get("keyStore").textValue();
            }
            String keyStorePassword = null;
            if (secu.hasNonNull("keyStorePassword")) {
                keyStorePassword = secu.get("keyStorePassword").textValue();
            }
            String privateKey = null;
            if (secu.hasNonNull("privateKey")) {
                privateKey = secu.get("privateKey").textValue();
            }
            JsonCredential credential = null;

            if (secu.hasNonNull("credential")) {
                JsonNode jcredential = secu.get("credential");
                String username = null;
                String password = null;
                if (jcredential.hasNonNull("username")) {
                    username = jcredential.get("username").textValue();
                    password = Optional.ofNullable(jcredential.get("password")).map(JsonNode::textValue).orElse("");
                    credential = new JsonCredential(username, password);
                }
            }
            securityInfo = new JsonSecurityInfo(credential, trustStore,trustStorePassword, keyStore, keyStorePassword, privateKey );
        }
        return new JsonTarget(url, properties, securityInfo, name);
    }
}
