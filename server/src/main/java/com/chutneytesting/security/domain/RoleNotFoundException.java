package com.chutneytesting.security.domain;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String roleName) {
        super("Role [" + roleName + "] cannot be found");
    }
}
