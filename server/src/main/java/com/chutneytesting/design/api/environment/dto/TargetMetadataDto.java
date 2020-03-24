package com.chutneytesting.design.api.environment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.engine.domain.environment.SecurityInfo;
import com.chutneytesting.engine.domain.environment.SecurityInfo.Credential;
import com.chutneytesting.engine.domain.environment.SecurityInfo.SecurityInfoBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TargetMetadataDto {
    public final String name;
    public final String url;
    public final List<Entry> properties;
    public final Optional<String> username;
    public final Optional<String> password;
    public final Optional<String> keyStore;
    public final Optional<String> keyStorePassword;
    public final Optional<String> privateKey;

    public TargetMetadataDto(@JsonProperty("name") String name,
                             @JsonProperty("url") String url,
                             @JsonProperty("properties") List<Entry> properties,
                             @JsonProperty("username") String username,
                             @JsonProperty("password") String password,
                             @JsonProperty("keyStore") String keyStore,
                             @JsonProperty("keyStorePassword") String keyStorePassword,
                             @JsonProperty("privateKey") String privateKey) {
        this.name = name.trim();
        this.url = url.trim();
        this.properties = nulltoEmpty(properties);
        this.username = emptyToNull(username);
        this.password = emptyToNull(password);
        this.keyStore = emptyToNull(keyStore);
        this.keyStorePassword = emptyToNull(keyStorePassword);
        this.privateKey = emptyToNull(privateKey);
    }

    public Target toTarget(String environment) {
        SecurityInfoBuilder securityInfo = SecurityInfo.builder()
            .keyStore(keyStore.orElse(null))
            .keyStorePassword(keyStorePassword.orElse(null))
            .privateKey(privateKey.orElse(null));
        if (username.isPresent() || password.isPresent()) {
            securityInfo.credential(Credential.of(username.orElse(""), password.orElse("")));
        }
        return Target.builder()
            .withId(Target.TargetId.of(name, environment))
            .withUrl(url)
            .withProperties(toMap(this.properties))
            .withSecurity(securityInfo.build())
            .build();
    }

    public static TargetMetadataDto from(Target target) {
        return new TargetMetadataDto(
            target.name,
            target.url,
            toEntryList(target.properties),
            // TODO - manage nulls
            Optional.ofNullable(target.security.credential).map(c -> c.username).orElse(null),
            Optional.ofNullable(target.security.credential).map(c -> c.password).orElse(null),
            Optional.ofNullable(target.security.keyStore).orElse(null),
            Optional.ofNullable(target.security.keyStorePassword).orElse(null),
            Optional.ofNullable(target.security.privateKey).orElse(null)
        );
    }

    private Optional<String> emptyToNull(String s) {
        return Optional.ofNullable("".equals(s) ? null : s);
    }

    private <T> List<T> nulltoEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private static List<Entry> toEntryList(Map<String, String> properties) {
        return properties.entrySet().stream()
            .map(e -> new Entry(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    private Map<String, String> toMap(List<Entry> properties) {
        return properties.stream().collect(Collectors.toMap(e -> e.key, e -> e.value));
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
