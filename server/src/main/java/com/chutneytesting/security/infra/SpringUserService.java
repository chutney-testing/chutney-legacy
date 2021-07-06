package com.chutneytesting.security.infra;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.CurrentUserNotFoundException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringUserService {

    public UserDto currentUser() {
        final Optional<Authentication> authentication = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
        return (UserDto) authentication
            .map(Authentication::getPrincipal)
            .orElseThrow(CurrentUserNotFoundException::new);
    }
}
