package com.chutneytesting.agent.domain;

import java.util.Objects;

public final class TargetId {
    public final String name;
    public final String environment;

    public TargetId(String name, String environment) {
        this.name = Objects.requireNonNull(name, "name");
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    public static TargetId of(String name, String env) {
        return new TargetId(name, env);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetId targetId = (TargetId) o;
        return name.equals(targetId.name) &&
            environment.equals(targetId.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, environment);
    }
}
