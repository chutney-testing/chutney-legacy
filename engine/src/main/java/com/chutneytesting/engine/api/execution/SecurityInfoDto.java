package com.chutneytesting.engine.api.execution;

import java.util.Objects;

public class SecurityInfoDto {
    public final CredentialDto credential;
    public final String trustStore;
    public final String trustStorePassword;
    public final String keyStore;
    public final String keyStorePassword;
    public final String privateKey;

    public SecurityInfoDto(CredentialDto credential, String trustStore, String trustStorePassword, String keyStore, String keyStorePassword, String privateKey) {
        this.credential = credential;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.privateKey = privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityInfoDto that = (SecurityInfoDto) o;
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

    @Override
    public String toString() {
        return "SecurityInfoDto{" +
            "credential=" + credential +
            ", trustStore='" + trustStore + '\'' +
            ", trustStorePassword='" + trustStorePassword + '\'' +
            ", keyStore='" + keyStore + '\'' +
            ", keyStorePassword='" + keyStorePassword + '\'' +
            ", privateKey='" + privateKey + '\'' +
            '}';
    }
}
