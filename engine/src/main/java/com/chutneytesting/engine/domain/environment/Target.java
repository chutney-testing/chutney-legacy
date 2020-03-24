package com.chutneytesting.engine.domain.environment;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Target {

    public final TargetId id;
    public final String url;
    public final URI uri;
    public final Map<String, String> properties;
    public final SecurityInfo security;
    public final List<NamedHostAndPort> agents;

    private Target(TargetId id, String url, Map<String, String> properties, SecurityInfo security, List<NamedHostAndPort> agents) {
        this.id = id;
        this.url = url;
        this.uri = getUrlAsURI(url);
        this.properties = properties;
        this.security = security;
        this.agents = agents;
    }

    public static TargetBuilder builder() {
        return new TargetBuilder();
    }

    public static class TargetBuilder {
        private TargetId id;
        private String url;
        private Map<String, String> properties;
        private List<NamedHostAndPort> agents;
        private SecurityInfo security;

        private TargetBuilder() {}

        public Target build() {
            return new Target(
                ofNullable(id).orElse(Target.TargetId.of("")),
                ofNullable(url).orElse(""),
                ofNullable(properties).orElse(Collections.emptyMap()),
                ofNullable(security).orElse(SecurityInfo.builder().build()),
                ofNullable(agents).orElse(emptyList())
            );
        }

        public TargetBuilder withId(TargetId id) {
            this.id = id;
            return this;
        }

        public TargetBuilder withId(String id) {
            this.id = Target.TargetId.of(id);
            return this;
        }

        public TargetBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public TargetBuilder withProperties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        public TargetBuilder copyOf(Target target) {
            this.id = target.id;
            this.url = target.url;
            this.properties = target.properties;
            this.agents = target.agents;
            this.security = target.security;
            return this;
        }

        public TargetBuilder withSecurity(SecurityInfo securityInfo) {
            this.security = securityInfo;
            return this;
        }

        public TargetBuilder withAgents(List<NamedHostAndPort> agents) {
            this.agents = agents;
            return this;
        }
    }

    public String name() {
        return this.id.name;
    }

    private URI getUrlAsURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class TargetId {
        public final String name;

        private TargetId(String name) {
            this.name = name;
        }

        static TargetId of(String name) {
            return new TargetId(name);
        }
    }

}
