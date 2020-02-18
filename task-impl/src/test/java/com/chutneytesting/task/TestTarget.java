package com.chutneytesting.task;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.HashMap;
import java.util.Map;

public class TestTarget implements Target {

    private final TargetId targetId;
    private final String url;
    private final Map<String, String> properties;
    private final SecurityInfo security;

    private TestTarget(TargetId targetId, String url, Map<String, String> properties, SecurityInfo security) {
        this.targetId = targetId;
        this.url = url;
        this.properties = (properties != null) ? properties : new HashMap<>();
        this.security = security;
    }

    @Override
    public TargetId id() {
        return targetId;
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
        return security;
    }

    static class TestTargetId implements TargetId {
        private final String name;

        TestTargetId(String name) {
            this.name = name;
        }

        static TargetId of(String id) {
            return new TestTargetId(id);
        }

        @Override
        public String name() {
            return name;
        }
    }

    public static final class TestTargetBuilder {
        private TargetId targetId;
        private String url;
        private Map<String, String> properties = new HashMap<>();
        private SecurityInfo security;

        private TestTargetBuilder() {
        }

        public static TestTargetBuilder builder() {
            return new TestTargetBuilder();
        }

        public TestTargetBuilder withTargetId(String targetId) {
            this.targetId = TestTargetId.of(targetId);
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

        public TestTargetBuilder withSecurity(String user, String password) {
            this.security = TestSecurityInfo.TestSecurityInfoBuilder.builder().withUsername(user).withPassword(password).build();
            return this;
        }

        public TestTargetBuilder withSecurity(SecurityInfo security) {
            this.security = security;
            return this;
        }

        public TestTarget build() {
            return new TestTarget(targetId, url, properties, security);
        }
    }
}
