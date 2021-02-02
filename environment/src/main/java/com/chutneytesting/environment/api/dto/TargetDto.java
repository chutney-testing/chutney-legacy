package com.chutneytesting.environment.api.dto;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.environment.domain.SecurityInfo;
import com.chutneytesting.environment.domain.Target;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TargetDto {
    public final String name;
    public final String url;
    public final List<Entry> properties;
    public final String username;
    public final String password;
    public final String keyStore;
    public final String keyStorePassword;
    public final String privateKey;

    public TargetDto(@JsonProperty("name") String name,
                     @JsonProperty("url") String url,
                     @JsonProperty("properties") List<Entry> properties,
                     @JsonProperty("username") String username,
                     @JsonProperty("password") String password,
                     @JsonProperty("keyStore") String keyStore,
                     @JsonProperty("keyStorePassword") String keyStorePassword,
                     @JsonProperty("privateKey") String privateKey) {
        this.name = name.trim();
        this.url = url.trim();
        this.properties = nullToEmpty(properties);
        this.username = emptyToNull(username);
        this.password = emptyToNull(password);
        this.keyStore = emptyToNull(keyStore);
        this.keyStorePassword = emptyToNull(keyStorePassword);
        this.privateKey = emptyToNull(privateKey);
    }

    public Target toTarget(String environment) {
        SecurityInfo.SecurityInfoBuilder securityInfo = SecurityInfo.builder()
            .keyStore(keyStore)
            .keyStorePassword(keyStorePassword)
            .privateKey(privateKey);
        if (username != null || password != null) {
            securityInfo.credential(SecurityInfo.Credential.of(username, password));
        }
        return Target.builder()
            .withName(name)
            .withEnvironment(environment)
            .withUrl(url)
            .withProperties(propertiesToMap())
            .withSecurity(securityInfo.build())
            .build();
    }

    public static TargetDto from(Target target) {
        return new TargetDto(
            target.name,
            target.url,
            toEntryList(target.properties),
            ofNullable(target.security.credential).map(c -> c.username).orElse(null),
            ofNullable(target.security.credential).map(c -> c.password).orElse(null),
            ofNullable(target.security.keyStore).orElse(null),
            ofNullable(target.security.keyStorePassword).orElse(null),
            ofNullable(target.security.privateKey).orElse(null)
        );
    }

    public Map<String, String> propertiesToMap() {
        return properties.stream().collect(Collectors.toMap(p -> p.key, p -> p.value));
    }

    public boolean hasCredential() {
        return !(isNullOrEmpty(username) && isNullOrEmpty(password));
    }

    private String emptyToNull(String s) {
        return "".equals(s) ? null : s;
    }

    private <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private static List<Entry> toEntryList(Map<String, String> properties) {
        return properties.entrySet().stream()
            .map(e -> new Entry(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    public static class Entry {
        public final String key;
        public final String value;

        public Entry(@JsonProperty("key") String key, @JsonProperty("value") String value) {
            this.key = key;
            this.value = value;
        }
    }
}
