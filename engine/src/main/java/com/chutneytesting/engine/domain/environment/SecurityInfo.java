package com.chutneytesting.engine.domain.environment;

import static java.util.Optional.ofNullable;

import java.util.Objects;

public class SecurityInfo {

    private Credential credential;
    private String trustStore;
    private String trustStorePassword;
    private String keyStore;
    private String keyStorePassword;
    private String privateKey;

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

    public Credential credential() {
        return hasCredential() ? credential : Credential.NONE;
    }

    public boolean hasCredential() {
        return ofNullable(credential).isPresent()
            && !Credential.NONE.equals(credential);
    }

    public String trustStore() {
        return ofNullable(trustStore).orElse("");
    }

    public boolean hasTrustStore() {
        return ofNullable(trustStore).isPresent()
            && !"".equals(trustStore);
    }

    public String trustStorePassword() {
        return ofNullable(trustStorePassword).orElse("");
    }

    public String keyStore() {
        return ofNullable(keyStore).orElse("");
    }

    public boolean hasKeyStore() {
        return ofNullable(keyStore).isPresent()
            && !"".equals(keyStore);
    }

    public String keyStorePassword() {
        return ofNullable(keyStorePassword).orElse("");
    }

    public String privateKey() {
        return ofNullable(privateKey).orElse("");
    }

    public boolean hasPrivateKey() {
        return ofNullable(privateKey).isPresent()
            && !"".equals(privateKey);
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
                credential,
                trustStore,
                trustStorePassword,
                keyStore,
                keyStorePassword,
                privateKey
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
        public static final Credential NONE = new NoCredential();

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

        private static class NoCredential extends Credential {
            private NoCredential() {
                super("", "");
            }
        }
    }
}
