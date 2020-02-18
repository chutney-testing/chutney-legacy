package com.chutneytesting.engine.domain.environment;

import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.Optional;

public class SecurityInfo {

    private Credential credential;
    private String trustStore;
    private String trustStorePassword;
    private String keyStore;
    private String keyStorePassword;
    private String privateKey;

    public SecurityInfo() {}

    public SecurityInfo(Credential credential, String trustStore, String trustStorePassword, String keyStore, String keyStorePassword, String privateKey) {
        this.credential = credential;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.privateKey = privateKey;
    }

    public static SecurityInfoBuilder builder() {
        return new SecurityInfoBuilder();
    }

    public Optional<Credential> credential() { return ofNullable(credential); }

    public Optional<String> trustStore() {
        return ofNullable(trustStore);
    }

    public Optional<String> trustStorePassword() {
        return ofNullable(trustStorePassword);
    }

    public Optional<String> keyStore() {
        return ofNullable(keyStore);
    }

    public Optional<String> keyStorePassword() {
        return ofNullable(keyStorePassword);
    }

    public Optional<String> privateKey() {
        return ofNullable(privateKey);
    }

    // Getter for jackson :
    public Credential getCredential() {
        return credential;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityInfo that = (SecurityInfo) o;
        return Objects.equals(credential, that.credential) &&
            Objects.equals(trustStore, that.trustStore) &&
            Objects.equals(trustStorePassword, that.trustStorePassword) &&
            Objects.equals(keyStore, that.keyStore) &&
            Objects.equals(keyStorePassword, that.keyStorePassword) &&
            Objects.equals(privateKey, that.privateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credential, trustStore, trustStorePassword, keyStore, keyStorePassword, privateKey);
    }

    public static final class SecurityInfoBuilder {
        private Credential credential;
        private String trustStore;
        private String trustStorePassword;
        private String keyStore;
        private String keyStorePassword;
        private String privateKey;

        private SecurityInfoBuilder() {
        }

        public SecurityInfoBuilder credential(Credential credential) {
            this.credential = credential;
            return this;
        }

        public SecurityInfoBuilder trustStore(String trustStore) {
            this.trustStore = trustStore;
            return this;
        }

        public SecurityInfoBuilder trustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
            return this;
        }

        public SecurityInfoBuilder keyStore(String keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        public SecurityInfoBuilder keyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        public SecurityInfoBuilder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public SecurityInfo build() {
            return new SecurityInfo(credential, trustStore, trustStorePassword, keyStore, keyStorePassword, privateKey);
        }
    }

    public static class Credential {
        private final String username;
        private final String password;

        private Credential(String username, String password) {
            // todo assert not null
            this.username = username;
            this.password = password;
        }

        public static Credential of(String username, String password) {
            return new Credential(username, password);
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String username() {
            return username;
        }

        public String password() {
            return password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Credential that = (Credential) o;
            return Objects.equals(username, that.username) &&
                Objects.equals(password, that.password);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, password);
        }
    }
}
