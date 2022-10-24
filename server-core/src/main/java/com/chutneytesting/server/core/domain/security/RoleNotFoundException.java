package com.chutneytesting.server.core.domain.security;

public class RoleNotFoundException extends RuntimeException {

    private RoleNotFoundException(String message) {
        super(message);
    }

    public static RoleNotFoundException forRole(String roleName) {
        return new RoleNotFoundException("Role [" + roleName + "] cannot be found");
    }

    public static RoleNotFoundException forUser(String userId) {
        return new RoleNotFoundException("No role defined for user " + userId);
    }
}
