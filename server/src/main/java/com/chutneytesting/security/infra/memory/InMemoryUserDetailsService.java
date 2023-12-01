/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.security.infra.memory;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
import com.chutneytesting.security.infra.UserDetailsServiceHelper;
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
            .map(u -> UserDetailsServiceHelper.grantAuthoritiesFromUserRole(u, authenticationService))
            .orElseThrow(() -> new UsernameNotFoundException("Username not found."));
    }
}
