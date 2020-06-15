package com.chutneytesting.security.domain;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserService {

    public User getCurrentUser(){
        final Optional<Authentication> authentication = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
        return (User) authentication
            .orElseThrow(CurrentUserNotFound::new)
            .getPrincipal();
    }
}
