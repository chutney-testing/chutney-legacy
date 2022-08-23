package com.chutneytesting.server.core.domain.security;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String roleName) {
        super("Role [" + roleName + "] cannot be found");
    }
}
