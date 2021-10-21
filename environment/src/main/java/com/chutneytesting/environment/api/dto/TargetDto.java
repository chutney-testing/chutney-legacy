package com.chutneytesting.environment.api.dto;

import static com.chutneytesting.tools.Entry.toEntryList;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.environment.domain.SecurityInfo;
import com.chutneytesting.environment.domain.Target;
import com.chutneytesting.tools.Entry;
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
    public final String keyPassword;
    public final String privateKey;

    public TargetDto(String name,
                     String url,
                     List<Entry> properties,
                     String username,
                     String password,
                     String keyStore,
                     String keyStorePassword,
                     String keyPassword,
                     String privateKey) {
        this.name = name.trim();
        this.url = url.trim();
        this.properties = nullToEmpty(properties);
        this.username = emptyToNull(username);
        this.password = emptyToNull(password);
        this.keyStore = emptyToNull(keyStore);
        this.keyStorePassword = emptyToNull(keyStorePassword);
        this.keyPassword = emptyToNull(keyPassword);
        this.privateKey = emptyToNull(privateKey);
    }

    public Target toTarget(String environment) {
        SecurityInfo.SecurityInfoBuilder securityInfo = SecurityInfo.builder()
            .keyStore(keyStore)
            .keyStorePassword(keyStorePassword)
            .keyPassword(keyPassword)
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
            ofNullable(target.security.keyPassword).orElse(null),
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
}
