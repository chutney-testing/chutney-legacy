package com.chutneytesting.engine.domain.environment;

import static java.util.Optional.ofNullable;

import java.util.Objects;

public class SecurityInfo {

    public final Credential credential;
    public final String trustStore;
    public final String trustStorePassword;
    public final String keyStore;
    public final String keyStorePassword;
    public final String privateKey;

    private SecurityInfo(Credential credential, String trustStore, String trustStorePassword, String keyStore, String keyStorePassword, String privateKey) {
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

        private SecurityInfoBuilder() {}

        public SecurityInfo build() {
            return new SecurityInfo(
                ofNullable(credential).orElse(Credential.NONE),
                ofNullable(trustStore).orElse(""),
                ofNullable(trustStorePassword).orElse(""),
                ofNullable(keyStore).orElse(""),
                ofNullable(keyStorePassword).orElse(""),
                ofNullable(privateKey).orElse("")
            );
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

    }

    public static class Credential {
        public static final Credential NONE = null;
        public final String username;
        public final String password;

        private Credential(String username, String password) {
            this.username = ofNullable(username).orElse("");
            this.password = ofNullable(password).orElse("");
        }

        public static Credential of(String username, String password) {
            return new Credential(username, password);
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
