package com.chutneytesting.engine.api.execution;

import java.util.Objects;

public class CredentialDto {
    public final String username;
    public final String password;

    public CredentialDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialDto that = (CredentialDto) o;
        return Objects.equals(username, that.username) &&
            Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}
