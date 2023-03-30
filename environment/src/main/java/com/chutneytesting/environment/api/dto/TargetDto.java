package com.chutneytesting.environment.api.dto;

import static com.chutneytesting.tools.Entry.toEntrySet;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import com.chutneytesting.environment.domain.Target;
import com.chutneytesting.tools.Entry;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TargetDto {
    public final String name;
    public final String url;
    public final String environment;
    public final Set<Entry> properties;

    public TargetDto() {
        this(null,null,null,null);
    }
    public TargetDto(String name,
                     String url,
                     Set<Entry> properties) {
        this(name, url, null, properties);
    }
    public TargetDto(String name,
                     String url,
                     String environment,
                     Set<Entry> properties) {
        this.name = trimIfNotNull(name);
        this.url = trimIfNotNull(url);
        this.environment = trimIfNotNull(environment);
        this.properties = nullToEmpty(properties);
    }

    public Target toTarget(String environment) {
        return Target.builder()
            .withName(name)
            .withEnvironment(environment)
            .withUrl(url)
            .withProperties(propertiesToMap())
            .build();
    }

    public Target toTarget() {
        return Target.builder()
            .withName(name)
            .withEnvironment(environment)
            .withUrl(url)
            .withProperties(propertiesToMap())
            .build();
    }

    public static TargetDto from(Target target) {
        return new TargetDto(
            target.name,
            target.url,
            target.environment,
            toEntrySet(target.properties)
        );
    }

    public Map<String, String> propertiesToMap() {
        return properties == null ? emptyMap() : properties.stream().collect(Collectors.toMap(p -> p.key, p -> p.value));
    }

    private <T> Set<T> nullToEmpty(Set<T> set) {
        return set == null ? emptySet() : set;
    }

    private String trimIfNotNull(String environment) {
        return environment != null ? environment.trim() : null;
    }
}
