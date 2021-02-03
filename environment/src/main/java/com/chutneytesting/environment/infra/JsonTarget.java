package com.chutneytesting.environment.infra;

import static java.util.Optional.ofNullable;

import com.chutneytesting.environment.domain.SecurityInfo;
import com.chutneytesting.environment.domain.Target;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;

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
        SecurityInfo securityInfo = null;
        if (security != null) {
            SecurityInfo.Credential cred = null;
            if (security.credential != null) {
                cred = SecurityInfo.Credential.of(security.credential.username, security.credential.password);
            }
            securityInfo = SecurityInfo.builder()
                .credential(cred)
                .keyStore(security.keyStore)
                .keyStorePassword(security.keyStorePassword)
                .trustStore(security.trustStore)
                .trustStorePassword(security.trustStorePassword)
                .privateKey(security.privateKey)
                .build();
        }
        return Target.builder()
            .withName(name)
            .withEnvironment(envName)
            .withUrl(url)
            .withProperties(properties)
            .withSecurity(securityInfo)
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
                ofNullable(security.credential).map(JsonCredential::from).orElse(null),
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

        public static JsonCredential from(SecurityInfo.Credential credential) {
            return new JsonCredential(credential.username, credential.password);
        }
    }
}
