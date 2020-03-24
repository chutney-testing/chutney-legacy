package com.chutneytesting.design.infra.storage.environment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.design.domain.environment.Target.TargetId;
import com.chutneytesting.engine.domain.environment.SecurityInfo;
import com.chutneytesting.engine.domain.environment.SecurityInfo.Credential;
import java.util.Map;
import java.util.Optional;

@JsonDeserialize(using = TargetJsonDeserializer.class)
public class JsonTarget {

    public String url;
    public Map<String, String> properties;
    public JsonSecurityInfo security;
    public String name;

    public JsonTarget() {
    }

    public JsonTarget(String url, Map<String, String> properties, JsonSecurityInfo security, String name) {
        this.url = url;
        this.properties = properties;
        this.security = security;
        this.name = name;
    }

    public static JsonTarget from(Target t) {
        return new JsonTarget(t.url,
            t.properties,
            JsonSecurityInfo.from(t.security),
            t.name
        );
    }

    public Target toTarget(String envName) {
        SecurityInfo secu = null;
        if(security != null) {
            Credential cred = null;
            if(security.credential != null) {
                cred = Credential.of(security.credential.username, security.credential.password);
            }
            secu = SecurityInfo.builder()
                .credential(cred)
                .keyStore(security.keyStore)
                .keyStorePassword(security.keyStorePassword)
                .trustStore(security.trustStore)
                .trustStorePassword(security.trustStorePassword)
                .privateKey(security.privateKey)
                .build();
        }
        return Target.builder()
            .withId(TargetId.of(name, envName))
            .withUrl(url)
            .withProperties(properties)
            .withSecurity(secu)
            .build();
    }

    public static class JsonSecurityInfo {

        public final JsonCredential credential;
        public final String trustStore;
        public final String trustStorePassword;
        public final String keyStore;
        public final String keyStorePassword;
        public final String privateKey;

        public JsonSecurityInfo(JsonCredential credential, String trustStore, String trustStorePassword, String keyStore, String keyStorePassword, String privateKey) {
            this.credential = credential;
            this.trustStore = trustStore;
            this.trustStorePassword = trustStorePassword;
            this.keyStore = keyStore;
            this.keyStorePassword = keyStorePassword;
            this.privateKey = privateKey;
        }

        public static JsonSecurityInfo from(SecurityInfo security) {
            return new JsonSecurityInfo(
                JsonCredential.from(security.credential()),
                security.trustStore,
                security.trustStorePassword,
                security.keyStore,
                security.keyStorePassword,
                security.privateKey
            );
        }
    }

    public static class JsonCredential {
        public final String username;
        public final String password;

        public JsonCredential(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public static JsonCredential from(Optional<Credential> credential) {
            if(credential.isPresent()) {
                return new JsonCredential(credential.get().username, credential.get().password);
            }
            return new JsonCredential("", "");
        }
    }
}
