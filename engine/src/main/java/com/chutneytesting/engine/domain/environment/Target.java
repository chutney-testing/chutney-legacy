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

    public static final Target NONE = Target.builder().build();

    public final String name;
    public final String url;
    public final URI uri;
    public final Map<String, String> properties;
    public final SecurityInfo security;
    public final List<NamedHostAndPort> agents;

    private Target(String name, String url, Map<String, String> properties, SecurityInfo security, List<NamedHostAndPort> agents) {
        this.name = name;
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
        private String name;
        private String url;
        private Map<String, String> properties;
        private List<NamedHostAndPort> agents;
        private SecurityInfo security;

        private TargetBuilder() {}

        public Target build() {
            return new Target(
                ofNullable(name).orElse(""),
                ofNullable(url).orElse(""),
                ofNullable(properties).orElse(Collections.emptyMap()),
                ofNullable(security).orElse(SecurityInfo.builder().build()),
                ofNullable(agents).orElse(emptyList())
            );
        }

        public TargetBuilder withName(String name) {
            this.name = name;
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
            this.name = target.name;
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
        return this.name;
    }

    private URI getUrlAsURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

}
