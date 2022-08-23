package com.chutneytesting.security.domain;

import com.chutneytesting.server.core.domain.security.UserRoles;

public interface Authorizations {

    UserRoles read();

    void save(UserRoles userRoles);
}
