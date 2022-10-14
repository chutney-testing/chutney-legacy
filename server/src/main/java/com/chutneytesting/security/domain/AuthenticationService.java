package com.chutneytesting.security.domain;

import com.chutneytesting.server.core.domain.security.Role;
import com.chutneytesting.server.core.domain.security.User;
import com.chutneytesting.server.core.domain.security.UserRoles;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AuthenticationService {

    private final Authorizations authorizations;

    public AuthenticationService(Authorizations authorizations) {
        this.authorizations = authorizations;
    }

    public Role userRoleById(String userId) {
        UserRoles userRoles = authorizations.read();
        User user = userRoles.userById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Username not found."));
        return userRoles.roleByName(user.roleName);
    }
}
