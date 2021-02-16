package com.chutneytesting.engine.api.execution;

import java.util.Objects;

public class CredentialExecutionDto {
    public final String username;
    public final String password;

    public CredentialExecutionDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialExecutionDto that = (CredentialExecutionDto) o;
        return Objects.equals(username, that.username) &&
            Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}
