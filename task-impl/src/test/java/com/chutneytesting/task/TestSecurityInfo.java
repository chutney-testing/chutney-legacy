package com.chutneytesting.task;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import java.util.Optional;

public class TestSecurityInfo implements SecurityInfo {

    private final Credential credential;

    private final String trustStore;
    private final String trustStorePassword;
    private final String privateKey;

    private TestSecurityInfo(Credential credential, String trustStore, String trustStorePassword, String privateKey) {
        this.credential = credential;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.privateKey = privateKey;
    }

    @Override
    public Optional<Credential> credential() {
        return Optional.ofNullable(credential);
    }

    @Override
    public Optional<String> trustStore() {
        return Optional.ofNullable(trustStore);
    }

    @Override
    public Optional<String> trustStorePassword() {
        return Optional.ofNullable(trustStorePassword);
    }

    @Override
    public Optional<String> keyStore() {
        return Optional.empty();
    }

    @Override
    public Optional<String> keyStorePassword() {
        return Optional.empty();
    }

    @Override
    public Optional<String> privateKey() {
        return Optional.ofNullable(privateKey);
    }

    private static class TestCredential implements SecurityInfo.Credential {
        private final String username;
        private final String password;

        TestCredential(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public String username() {
            return username;
        }

        @Override
        public String password() {
            return password;
        }
    }

    public static final class TestSecurityInfoBuilder {
        private String username;
        private String password;
        private String trustStore;
        private String trustStorePassword;
        private String privateKey;

        private TestSecurityInfoBuilder() {
        }

        public static TestSecurityInfoBuilder builder() {
            return new TestSecurityInfoBuilder();
        }

        public TestSecurityInfoBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public TestSecurityInfoBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public TestSecurityInfoBuilder withTrustStore(String trustStore) {
            this.trustStore = trustStore;
            return this;
        }

        public TestSecurityInfoBuilder withTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
            return this;
        }

        public TestSecurityInfo build() {
            return new TestSecurityInfo(new TestCredential(username, password), trustStore, trustStorePassword, privateKey);
        }

        public TestSecurityInfoBuilder withPrivateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }
    }
}
