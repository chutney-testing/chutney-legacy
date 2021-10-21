package com.chutneytesting.engine.api.execution;

import java.util.Objects;

public class SecurityInfoExecutionDto {
    public final CredentialExecutionDto credential;
    public final String trustStore;
    public final String trustStorePassword;
    public final String keyStore;
    public final String keyStorePassword;
    public final String keyPassword;
    public final String privateKey;

    public SecurityInfoExecutionDto(CredentialExecutionDto credential, String trustStore, String trustStorePassword, String keyStore, String keyStorePassword, String keyPassword, String privateKey) {
        this.credential = credential;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.keyPassword = keyPassword;
        this.privateKey = privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityInfoExecutionDto that = (SecurityInfoExecutionDto) o;
        return Objects.equals(credential, that.credential) &&
            Objects.equals(trustStore, that.trustStore) &&
            Objects.equals(trustStorePassword, that.trustStorePassword) &&
            Objects.equals(keyStore, that.keyStore) &&
            Objects.equals(keyStorePassword, that.keyStorePassword) &&
            Objects.equals(keyPassword, that.keyPassword) &&
            Objects.equals(privateKey, that.privateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credential, trustStore, trustStorePassword, keyStore, keyStorePassword, keyPassword, privateKey);
    }

    @Override
    public String toString() {
        return "SecurityInfoDto{" +
            "credential=" + credential +
            ", trustStore='" + trustStore + '\'' +
            ", trustStorePassword='" + trustStorePassword + '\'' +
            ", keyStore='" + keyStore + '\'' +
            ", keyStorePassword='" + keyStorePassword + '\'' +
            ", keyPassword='" + keyPassword + '\'' +
            ", privateKey='" + privateKey + '\'' +
            '}';
    }
}
