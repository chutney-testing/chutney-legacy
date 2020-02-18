package com.chutneytesting.engine.domain.environment;

import static com.chutneytesting.engine.domain.environment.SecurityInfo.SecurityInfoBuilder;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

// TODO file/http model must be placed in appropriate space without coupling to domain-used one {@link Target}
public class TargetJsonDeserializer extends JsonDeserializer<Target> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Target deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        JsonNode targetNode = jsonParser.getCodec().readTree(jsonParser);
        ImmutableTarget.Builder targetBuilder = ImmutableTarget.builder();

        if (targetNode.hasNonNull("name")) {
            targetBuilder.id(Target.TargetId.of(targetNode.get("name").textValue()));
        }
        if (targetNode.hasNonNull("url")) {
            targetBuilder.url(targetNode.get("url").textValue());
        }
        if (targetNode.hasNonNull("properties")) {
            targetBuilder.putAllProperties(mapper.readValue(targetNode.get("properties").toString(), new TypeReference<Map<String, ? extends String>>() {
            }));
        }
        if (targetNode.hasNonNull("security")) {
            JsonNode secu = targetNode.get("security");
            SecurityInfoBuilder secuBuilder = SecurityInfo.builder();
            if (secu.hasNonNull("trustStore")) {
                secuBuilder.trustStore(secu.get("trustStore").textValue());
            }
            if (secu.hasNonNull("trustStorePassword")) {
                secuBuilder.trustStorePassword(secu.get("trustStorePassword").textValue());
            }
            if (secu.hasNonNull("keyStore")) {
                secuBuilder.keyStore(secu.get("keyStore").textValue());
            }
            if (secu.hasNonNull("keyStorePassword")) {
                secuBuilder.keyStorePassword(secu.get("keyStorePassword").textValue());
            }
            if (secu.hasNonNull("privateKey")) {
                secuBuilder.privateKey(secu.get("privateKey").textValue());
            }
            if (secu.hasNonNull("credential")) {
                JsonNode credential = secu.get("credential");
                if (credential.hasNonNull("username")) {
                    String username = credential.get("username").textValue();
                    String password = ofNullable(credential.get("password")).map(JsonNode::textValue).orElse("");
                    secuBuilder.credential(SecurityInfo.Credential.of(username, password));
                }
            }
            targetBuilder.security(secuBuilder.build());
        }
        return targetBuilder.build();
    }
}
