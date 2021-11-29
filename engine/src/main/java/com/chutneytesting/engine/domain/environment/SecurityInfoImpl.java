package com.chutneytesting.engine.domain.environment;

import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import java.util.Objects;
import java.util.Optional;

public class SecurityInfoImpl implements SecurityInfo {

    private Credential credential;
    private String trustStore;
    private String trustStorePassword;
    private String keyStore;
    private String keyStorePassword;
    private String keyPassword;
    private String privateKey;

    private SecurityInfoImpl(Credential credential, String trustStore, String trustStorePassword, String keyStore, String keyStorePassword, String keyPassword, String privateKey) {
        this.credential = credential;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.keyPassword = keyPassword;
        this.privateKey = privateKey;
    }

    public static SecurityInfoBuilder builder() {
        return new SecurityInfoBuilder();
    }

    @Override
    public Optional<SecurityInfo.Credential> credential() {
        return hasCredential() ? Optional.of(credential) : Optional.empty();
    }

    public boolean hasCredential() {
        return ofNullable(credential).isPresent()
            && !Credential.NONE.equals(credential);
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

    public boolean hasTrustStore() {
        return ofNullable(trustStore).isPresent()
            && !"".equals(trustStore);
    }

    public boolean hasKeyStore() {
        return ofNullable(keyStore).isPresent()
            && !"".equals(keyStore);
    }

    public boolean hasPrivateKey() {
        return ofNullable(privateKey).isPresent()
            && !"".equals(privateKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityInfoImpl that = (SecurityInfoImpl) o;
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
        private String keyPassword;
        private String privateKey;

        private SecurityInfoBuilder() {}

        public SecurityInfoImpl build() {
            return new SecurityInfoImpl(
                credential,
                trustStore,
                trustStorePassword,
                keyStore,
                keyStorePassword,
                keyPassword,
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

        public SecurityInfoBuilder keyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        public SecurityInfoBuilder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }
    }

    public static class Credential implements SecurityInfo.Credential {
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

        @Override
        public String username() {
            return username;
        }

        @Override
        public String password() {
            return password;
        }

        private static class NoCredential extends Credential {
            private NoCredential() {
                super("", "");
            }
        }
    }
}
