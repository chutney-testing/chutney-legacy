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

package com.chutneytesting;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.server.core.domain.security.Authorization;
import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityConfiguration {

    @Bean("unsecureFilterChain")
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        UserDto defaultUser = getDefaultUser();

        http
            .csrf(AbstractHttpConfigurer::disable)
            .anonymous(anonymousConfigurer -> anonymousConfigurer
                .principal(defaultUser)
                .authorities(new ArrayList<>(defaultUser.getAuthorities())))
            .authorizeHttpRequests(httpRequest -> httpRequest.anyRequest().permitAll());

        http
            .requiresChannel(channelRequestMatcherRegistry -> channelRequestMatcherRegistry.anyRequest().requiresInsecure());

        return http.build();
    }

    protected UserDto getDefaultUser() {
        UserDto defaultUser = new UserDto();
        defaultUser.setName("ChutneyPluginUser");
        defaultUser.addRole("ADMIN");
        Arrays.stream(Authorization.values()).map(Enum::name).forEach(defaultUser::grantAuthority);
        return defaultUser;
    }
}
