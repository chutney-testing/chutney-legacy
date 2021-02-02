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
    public final SecurityInfo security;
    public final String name;
    public final String environment;

    private Target(String environment, String url, Map<String, String> properties, SecurityInfo security, String name) {
        this.environment = environment;
        this.url = url;
        this.properties = properties;
        this.security = security;
        this.name = name;
    }

    public static TargetBuilder builder() {
        return new TargetBuilder();
    }

    public static class TargetBuilder {
        private String name;
        private String environment;
        private String url;
        private Map<String, String> properties;
        private SecurityInfo security;

        private TargetBuilder() {
        }

        public Target build() {
            return new Target(
                Objects.requireNonNull(environment, "environment"),
                Objects.requireNonNull(url, "url"),
                unmodifiableMap(ofNullable(properties).orElse(emptyMap())),
                ofNullable(security).orElse(SecurityInfo.builder().build()),
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
            this.properties = new HashMap<>(value);
            return this;
        }

        public TargetBuilder withSecurity(SecurityInfo security) {
            this.security = Objects.requireNonNull(security, "security");
            return this;
        }

        public TargetBuilder copyOf(Target target) {
            Objects.requireNonNull(target, "target");
            withName(target.name);
            withEnvironment(target.environment);
            withUrl(target.url);
            withSecurity(target.security);
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
            Objects.equals(security, target.security) &&
            Objects.equals(name, target.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment, url, properties, security, name);
    }

    @Override
    public String toString() {
        return "Target{" +
            "environment=" + environment +
            ", url='" + url + '\'' +
            ", properties=" + properties +
            ", security=" + security +
            ", name='" + name + '\'' +
            '}';
    }
}
