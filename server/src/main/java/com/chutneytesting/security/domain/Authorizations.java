package com.chutneytesting.security.domain;

public interface Authorizations {

    UserRoles read();

    void save(UserRoles userRoles);
}
