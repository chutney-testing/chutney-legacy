package com.chutneytesting.task.spi.injectable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public interface Target {

    TargetId id();

    String url();

    Map<String, String> properties();

    SecurityInfo security();

    default String name() {
        return this.id().name();
    }

    default URI getUrlAsURI() {
        try {
            return new URI(url());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    interface TargetId {
        String name();
    }
}
