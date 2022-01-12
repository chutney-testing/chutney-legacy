package com.chutneytesting.task.spi.injectable;

import java.net.URI;
import java.util.Map;

public interface Target {

    String name();

    String url();

    Map<String, String> properties();

    SecurityInfo security();

    URI uri();

    default String host() {
        return uri().getHost();
    }

    default int port() {
        return uri().getPort();
    }

}
