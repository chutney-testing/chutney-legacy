package com.chutneytesting.design.domain.environment;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.engine.domain.environment.SecurityInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Target {

    public final TargetId id;
    public final String url;
    public final Map<String, String> properties;
    public final SecurityInfo security;
    public final String name;
    public final List<NamedHostAndPort> agents;

    private Target(TargetId id, String url, Map<String, String> properties, SecurityInfo security, List<NamedHostAndPort> agents, String name) {
        this.id = id;
        this.url = url;
        this.properties = properties;
        this.security = security;
        this.name = name;
        this.agents = agents;
    }

    public static TargetBuilder builder() {
        return new TargetBuilder();
    }

    public static class TargetBuilder {
        private TargetId id;
        private String url;
        private Map<String, String> properties;
        private SecurityInfo security;
        private List<NamedHostAndPort> agents;

        private TargetBuilder() {
        }

        public Target build() {
            return new Target(
                Objects.requireNonNull(id, "id"),
                Objects.requireNonNull(url, "url"),
                unmodifiableMap(ofNullable(properties).orElse(emptyMap())),
                ofNullable(security).orElse(SecurityInfo.builder().build()),
                unmodifiableList(ofNullable(agents).orElse(emptyList())),
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

        public TargetBuilder withAgents(List<NamedHostAndPort> agents) {
            this.agents = new ArrayList<>(agents);
            return this;
        }

        public TargetBuilder copyOf(Target target) {
            Objects.requireNonNull(target, "target");
            withId(target.id);
            withUrl(target.url);
            withSecurity(target.security);
            withProperties(target.properties);
            withAgents(target.agents);
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
            Objects.equals(name, target.name) &&
            Objects.equals(agents, target.agents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, properties, security, name, agents);
    }

    @Override
    public String toString() {
        return "Target{" +
            "id=" + id +
            ", url='" + url + '\'' +
            ", properties=" + properties +
            ", security=" + security +
            ", name='" + name + '\'' +
            ", agents=" + agents +
            '}';
    }
}
