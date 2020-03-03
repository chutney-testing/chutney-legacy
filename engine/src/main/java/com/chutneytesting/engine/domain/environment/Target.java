package com.chutneytesting.engine.domain.environment;

import static com.chutneytesting.engine.domain.environment.SecurityInfo.builder;

import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@Value.Enclosing
@JsonSerialize(as = ImmutableTarget.class)
@JsonDeserialize(using = TargetJsonDeserializer.class)
public interface Target {

    @Value.Parameter
    @JsonIgnore
    TargetId id();

    @Value.Parameter
    String url();

    Map<String, String> properties();

    @Value.Default
    default SecurityInfo security() {
        return builder().build();
    }

    @Value.Derived
    default String name() {
        return this.id().name();
    }

    @Value.Parameter
    Optional<List<NamedHostAndPort>> agents();

    @Value.Derived
    @JsonIgnore
    default URI getUrlAsURI() {
        try {
            return new URI(url());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Value.Immutable
    interface TargetId {
        @Value.Parameter
        String name();

        static TargetId of(String name) {
            return ImmutableTarget.TargetId.of(name);
        }
    }
}
