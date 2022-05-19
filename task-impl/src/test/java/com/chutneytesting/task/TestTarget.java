package com.chutneytesting.task;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TestTarget implements Target {

    private final String name;
    private final String url;
    private final Map<String, String> properties;

    private TestTarget(String name, String url, Map<String, String> properties) {
        this.name = name;
        this.url = url;
        this.properties = (properties != null) ? properties : new HashMap<>();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public SecurityInfo security() {
        throw new IllegalCallerException();
    }

    @Override
    public URI uri() {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class TestTargetBuilder {
        private String name;
        private String url;
        private final Map<String, String> properties = new HashMap<>();

        private TestTargetBuilder() {
        }

        public static TestTargetBuilder builder() {
            return new TestTargetBuilder();
        }

        public TestTargetBuilder withTargetId(String name) {
            this.name = name;
            return this;
        }

        public TestTargetBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public TestTargetBuilder withProperty(String key, String value) {
            this.properties.put(key, value);
            return this;
        }

        public TestTarget build() {
            return new TestTarget(name, url, properties);
        }
    }
}
