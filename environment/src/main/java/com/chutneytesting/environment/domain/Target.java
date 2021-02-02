package com.chutneytesting.environment.domain;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Target {

    public final TargetId id;
    public final String url;
    public final Map<String, String> properties;
    public final SecurityInfo security;
    public final String name;

    private Target(TargetId id, String url, Map<String, String> properties, SecurityInfo security, String name) {
        this.id = id;
        this.url = url;
        this.properties = properties;
        this.security = security;
        this.name = name;
    }

    public static TargetBuilder builder() {
        return new TargetBuilder();
    }

    public static class TargetBuilder {
        private TargetId id;
        private String url;
        private Map<String, String> properties;
        private SecurityInfo security;

        private TargetBuilder() {
        }

        public Target build() {
            return new Target(
                Objects.requireNonNull(id, "id"),
                Objects.requireNonNull(url, "url"),
                unmodifiableMap(ofNullable(properties).orElse(emptyMap())),
                ofNullable(security).orElse(SecurityInfo.builder().build()),
                Objects.requireNonNull(id.name, "name")
            );
        }

        public TargetBuilder withId(TargetId value) {
            this.id = Objects.requireNonNull(value, "id");
            return this;
        }

        public TargetBuilder withUrl(String value) {
            this.url = Objects.requireNonNull(value, "url");
            return this;
        }

        public TargetBuilder withProperties(Map<String, String> value) {
            this.properties = new HashMap<>(value);
            return this;
        }

        public TargetBuilder withSecurity(SecurityInfo security) {
            this.security = Objects.requireNonNull(security, "security");
            return this;
        }

        public TargetBuilder copyOf(Target target) {
            Objects.requireNonNull(target, "target");
            withId(target.id);
            withUrl(target.url);
            withSecurity(target.security);
            withProperties(target.properties);
            return this;
        }
    }

    public static final class TargetId {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equals(id, target.id) &&
            Objects.equals(url, target.url) &&
            Objects.equals(properties, target.properties) &&
            Objects.equals(security, target.security) &&
            Objects.equals(name, target.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, properties, security, name);
    }

    @Override
    public String toString() {
        return "Target{" +
            "id=" + id +
            ", url='" + url + '\'' +
            ", properties=" + properties +
            ", security=" + security +
            ", name='" + name + '\'' +
            '}';
    }
}
