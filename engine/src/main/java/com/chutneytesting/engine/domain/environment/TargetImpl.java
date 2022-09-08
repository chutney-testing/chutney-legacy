package com.chutneytesting.engine.domain.environment;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.action.spi.injectable.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TargetImpl implements Target {

    public static final TargetImpl NONE = TargetImpl.builder().build();

    public final String name;
    public final String url;
    public final Map<String, String> properties;
    public final List<NamedHostAndPort> agents;

    private TargetImpl(String name, String url, Map<String, String> properties, List<NamedHostAndPort> agents) {
        this.name = name;
        this.url = url;
        this.properties = properties;
        this.agents = agents;
    }

    public static TargetBuilder builder() {
        return new TargetBuilder();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public URI uri() {
        return uriFrom(url);
    }

    @Override
    public String rawUri() {
        return url;
    }

    @Override
    public Optional<String> property(String key) {
        return ofNullable(properties.get(key));
    }

    @Override
    public Map<String, String> prefixedProperties(String prefix, boolean cutPrefix) {
        return properties.entrySet().stream()
            .filter(e -> e.getKey() != null)
            .filter(e -> e.getKey().startsWith(prefix))
            .collect(toMap(e -> e.getKey().substring(cutPrefix ? prefix.length() : 0), Map.Entry::getValue));
    }

    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public String toString() {
        return "TargetImpl{" +
            "name='" + name + '\'' +
            ", url='" + url + '\'' +
            ", properties=" + properties +
            '}';
    }

    public static class TargetBuilder {
        private String name;
        private String url;
        private Map<String, String> properties;
        private List<NamedHostAndPort> agents;

        private TargetBuilder() {
        }

        public TargetImpl build() {
            return new TargetImpl(
                ofNullable(name).orElse(""),
                ofNullable(url).orElse(""),
                ofNullable(properties).orElse(Collections.emptyMap()),
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

        public TargetBuilder copyOf(TargetImpl target) {
            this.name = target.name;
            this.url = target.uri().toString();
            this.properties = target.properties;
            this.agents = target.agents;
            return this;
        }

        public TargetBuilder withAgents(List<NamedHostAndPort> agents) {
            this.agents = agents;
            return this;
        }
    }

    private URI uriFrom(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
