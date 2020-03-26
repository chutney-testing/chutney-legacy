package com.chutneytesting.task.spi.injectable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public interface Target {

    String name();

    String url();

    Map<String, String> properties();

    SecurityInfo security();

    default URI getUrlAsURI() {
        try {
            return new URI(url());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

}
