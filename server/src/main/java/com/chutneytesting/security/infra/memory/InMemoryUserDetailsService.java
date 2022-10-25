package com.chutneytesting.security.infra.memory;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
import com.chutneytesting.server.core.domain.security.Authorization;
import com.chutneytesting.server.core.domain.security.Role;
import com.chutneytesting.server.core.domain.security.RoleNotFoundException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class InMemoryUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryUserDetailsService.class);
    private final Map<String, UserDto> users;
    private final AuthenticationService authenticationService;

    public InMemoryUserDetailsService(InMemoryUsersProperties inMemoryUsersProperties, AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        users = inMemoryUsersProperties.getUsers().stream()
            .collect(toUnmodifiableMap(UserDto::getUsername, identity()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDto> user = Optional.ofNullable(users.get(username));
        return user
            .map(this::readRole)
            .orElseThrow(() -> new UsernameNotFoundException("Username not found."));
    }

    private UserDto readRole(UserDto userDto) {
        UserDto dto = new UserDto(userDto);

        if (dto.getRoles().contains("ADMIN")) {
            Arrays.stream(Authorization.values()).map(Authorization::name).forEach(dto::grantAuthority);
        } else {
            try {
                Role role = authenticationService.userRoleById(dto.getId());
                dto.addRole(role.name);
                role.authorizations.stream().map(Enum::name).forEach(dto::grantAuthority);
            } catch (RoleNotFoundException rnfe) {
                LOGGER.warn("User {} has no role defined", dto.getId());
                throw rnfe;
            }
        }

        return dto;
    }
}
