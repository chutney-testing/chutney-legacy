package com.chutneytesting.security.infra.memory;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
import com.chutneytesting.security.domain.Authorization;
import com.chutneytesting.security.domain.Role;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class InMemoryUserDetailsService implements UserDetailsService {

    private final Map<String, UserDto> users = new HashMap<>();
    private final AuthenticationService authenticationService;

    public InMemoryUserDetailsService(InMemoryUsersProperties inMemoryUsersProperties, AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        inMemoryUsersProperties.getUsers().forEach(user -> users.put(user.getUsername(), user));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDto> user = Optional.ofNullable(users.get(username));
        return user
            .map(this::readRole)
            .orElseThrow(() -> new UsernameNotFoundException("Username not found."));
    }

    private UserDto readRole(UserDto userDto) {
        if (userDto.getRoles().contains("ADMIN")) {
            userDto.grantAuthority(Authorization.ADMIN_ACCESS.name());
        }

        Role role = authenticationService.userRoleById(userDto.getId());
        userDto.addRole(role.name);
        role.authorizations.stream().map(Enum::name).forEach(userDto::grantAuthority);

        return userDto;
    }
}
