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
