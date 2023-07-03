package com.chutneytesting.environment.domain;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Target {

    public final String url;
    public final Map<String, String> properties;
    public final String name;
    public final String environment;

    private Target(String environment, String url, Map<String, String> properties, String name) {
        this.environment = environment;
        this.url = url;
        this.properties = properties;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TargetBuilder builder() {
        return new TargetBuilder();
    }

    public static class TargetBuilder {
        private String name;
        private String environment;
        private String url;
        private Map<String, String> properties = new HashMap<>();

        private TargetBuilder() {
        }

        public Target build() {
            return new Target(
                Objects.requireNonNull(environment, "environment"),
                Objects.requireNonNull(url, "url"),
                unmodifiableMap(ofNullable(properties).orElse(emptyMap())),
                Objects.requireNonNull(name, "name")
            );
        }

        public TargetBuilder withName(String value) {
            this.name = Objects.requireNonNull(value, "name");
            return this;
        }

        public TargetBuilder withEnvironment(String value) {
            this.environment = Objects.requireNonNull(value, "environment");
            return this;
        }

        public TargetBuilder withUrl(String value) {
            this.url = Objects.requireNonNull(value, "url");
            return this;
        }

        public TargetBuilder withProperties(Map<String, String> value) {
            this.properties.putAll(value);
            return this;
        }

        public TargetBuilder withProperty(String key, String value) {
            this.properties.put(key, value);
            return this;
        }

        public TargetBuilder withProperty(Map.Entry<String, String> entry) {
            this.properties.put(entry.getKey(), entry.getValue());
            return this;
        }

        public TargetBuilder copyOf(Target target) {
            Objects.requireNonNull(target, "target");
            withName(target.name);
            withEnvironment(target.environment);
            withUrl(target.url);
            withProperties(target.properties);
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equals(environment, target.environment) &&
            Objects.equals(url, target.url) &&
            Objects.equals(properties, target.properties) &&
            Objects.equals(name, target.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment, url, properties, name);
    }

    @Override
    public String toString() {
        return "Target{" +
            "environment=" + environment +
            ", url='" + url + '\'' +
            ", properties=" + properties +
            ", name='" + name + '\'' +
            '}';
    }
}
