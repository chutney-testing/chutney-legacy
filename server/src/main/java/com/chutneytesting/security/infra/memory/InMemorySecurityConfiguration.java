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

import com.chutneytesting.security.domain.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("mem-auth")
public class InMemorySecurityConfiguration {

    @Bean
    @ConfigurationProperties("chutney.security")
    public InMemoryUsersProperties users() {
        return new InMemoryUsersProperties();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsService inMemoryUserDetailsService(InMemoryUsersProperties users, AuthenticationService authenticationService) {
        return new InMemoryUserDetailsService(users, authenticationService);
    }

    @Configuration
    @Profile("mem-auth")
    public static class UserMemoryConfiguration {

        @Autowired
        protected void configure(
            final AuthenticationManagerBuilder auth,
            final PasswordEncoder pwdEncoder,
            final InMemoryUserDetailsService authService
        ) throws Exception {
            auth
                .userDetailsService(authService)
                .passwordEncoder(pwdEncoder);
        }
    }
}
