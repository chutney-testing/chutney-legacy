package com.chutneytesting.security.domain;

import com.chutneytesting.server.core.security.UserRoles;

public interface Authorizations {

    UserRoles read();

    void save(UserRoles userRoles);
}
