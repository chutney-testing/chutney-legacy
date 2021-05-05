package com.chutneytesting.security.domain;

public class AuthenticationService {

    private final Authorizations authorizations;

    public AuthenticationService(Authorizations authorizations) {
        this.authorizations = authorizations;
    }

    public Role userRoleById(String userId) {
        UserRoles userRoles = authorizations.read();
        User user = userRoles.userById(userId)
            .orElseGet(() -> {
                User newUser = userRoles.addNewUser(userId);
                authorizations.save(userRoles);
                return newUser;
            });
        return userRoles.roleByName(user.roleName);
    }
}
