package com.chutneytesting.engine.domain.environment;

import static com.chutneytesting.engine.domain.environment.SecurityInfo.builder;

import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@Value.Enclosing
public interface Target {

    @Value.Parameter
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
