package com.chutneytesting.environment.infra;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import com.chutneytesting.environment.domain.Environment;
import java.util.Set;

public class JsonEnvironment {

    public String name;
    public String description;
    public Set<JsonTarget> targets;

    public JsonEnvironment() {
    }

    private JsonEnvironment(String name, String description, Set<JsonTarget> targets) {
        this.name = name;
        this.description = description;
        this.targets = targets;
    }

    public static JsonEnvironment from(Environment environment) {
        Set<JsonTarget> targets = environment.targets.stream().map(JsonTarget::from).collect(toSet());
        return new JsonEnvironment(environment.name, environment.description, targets);
    }

    public Environment toEnvironment() {

        return Environment.builder()
            .withName(name)
            .withDescription(description)
            .withTargets(ofNullable(targets).orElse(emptySet()).stream().map(t -> t.toTarget(name)).collect(toSet()))
            .build();
    }
}
