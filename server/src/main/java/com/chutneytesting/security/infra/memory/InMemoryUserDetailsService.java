package com.chutneytesting.security.infra.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class InMemoryUserDetailsService implements UserDetailsService {

    private final Map<String, UserDetails> users = new HashMap<>();

    public InMemoryUserDetailsService(InMemoryUsersProperties inMemoryUsersProperties) {
        inMemoryUsersProperties.getUsers().forEach(user -> users.put(user.getUsername(), user));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDetails> user = Optional.ofNullable(users.get(username));
        return user.orElseThrow(() -> new UsernameNotFoundException("Username not found."));
    }
}
