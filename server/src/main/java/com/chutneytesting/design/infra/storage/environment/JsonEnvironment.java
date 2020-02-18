package com.chutneytesting.design.infra.storage.environment;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.environment.Environment;
import java.util.List;
import java.util.stream.Collectors;

public class JsonEnvironment {

    public String name;
    public String description;
    public List<JsonTarget> targets;

    public JsonEnvironment() {
    }

    public JsonEnvironment(String name, String description, List<JsonTarget> targets) {
        this.name = name;
        this.description = description;
        this.targets = targets;
    }

    public static JsonEnvironment from(Environment environment) {
        List<JsonTarget> targets = environment.targets.stream().map(t -> JsonTarget.from(t)).collect(Collectors.toList());
        return new JsonEnvironment(environment.name, environment.description, targets);
    }

    public Environment toEnvironment() {

        return Environment.builder()
            .withName(name)
            .withDescription(description)
            .withTargets(ofNullable(targets).orElse(emptyList()).stream().map(t -> t.toTarget(name)).collect(Collectors.toSet()))
            .build();
    }
}
