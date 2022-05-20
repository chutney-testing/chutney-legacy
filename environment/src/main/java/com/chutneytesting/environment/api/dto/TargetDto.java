package com.chutneytesting.environment.api.dto;

import static com.chutneytesting.tools.Entry.toEntrySet;
import static java.util.Collections.emptySet;

import com.chutneytesting.environment.domain.Target;
import com.chutneytesting.tools.Entry;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TargetDto {
    public final String name;
    public final String url;
    public final Set<Entry> properties;

    public TargetDto(String name,
                     String url,
                     Set<Entry> properties) {
        this.name = name.trim();
        this.url = url.trim();
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

    public static TargetDto from(Target target) {
        return new TargetDto(
            target.name,
            target.url,
            toEntrySet(target.properties)
        );
    }

    public Map<String, String> propertiesToMap() {
        return properties.stream().collect(Collectors.toMap(p -> p.key, p -> p.value));
    }

    private <T> Set<T> nullToEmpty(Set<T> set) {
        return set == null ? emptySet() : set;
    }
}
