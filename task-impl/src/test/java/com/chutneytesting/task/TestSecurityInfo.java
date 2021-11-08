package com.chutneytesting.task;

import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import java.util.Optional;

public class TestSecurityInfo implements SecurityInfo {

    private final Credential credential;

    private final String trustStore;
    private final String trustStorePassword;
    private final String keyStore;
    private final String keyStorePassword;
    private final String keyPassword;
    private final String privateKey;

    private TestSecurityInfo(Credential credential, String trustStore, String trustStorePassword, String keyStore, String keyStorePassword, String keyPassword, String privateKey) {
        this.credential = credential;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.keyPassword = keyPassword;
        this.privateKey = privateKey;
    }

    public static TestSecurityInfoBuilder builder() {
        return new TestSecurityInfoBuilder();
    }

    @Override
    public Optional<Credential> credential() {
        return ofNullable(credential);
    }

    @Override
    public Optional<String> trustStore() {
        return ofNullable(trustStore);
    }

    @Override
    public Optional<String> trustStorePassword() {
        return ofNullable(trustStorePassword);
    }

    @Override
    public Optional<String> keyStore() {
        return ofNullable(keyStore);
    }

    @Override
    public Optional<String> keyStorePassword() {
        return ofNullable(keyStorePassword);
    }

    @Override
    public Optional<String> keyPassword() {
        return ofNullable(keyPassword);
    }

    @Override
    public Optional<String> privateKey() {
        return ofNullable(privateKey);
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
        private String keyStore;
        private String keyStorePassword;
        private String privateKey;
        private String keyPassword;

        private TestSecurityInfoBuilder() {
        }

        public TestSecurityInfo build() {
            return new TestSecurityInfo(new TestCredential(username, password), trustStore, trustStorePassword, keyStore, keyStorePassword, keyPassword, privateKey);
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

        public TestSecurityInfoBuilder withKeyStore(String keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        public TestSecurityInfoBuilder withKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        public TestSecurityInfoBuilder withKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        public TestSecurityInfoBuilder withPrivateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }
    }
}
