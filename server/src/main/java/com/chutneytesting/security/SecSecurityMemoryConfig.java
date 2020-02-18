package com.chutneytesting.security;

import com.chutneytesting.security.infra.memory.InMemoryConfiguration;
import com.chutneytesting.security.infra.memory.InMemoryUsersProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@Profile("mem-auth")
public class SecSecurityMemoryConfig extends SecSecurityUILoginAbstractConfig {

    @Autowired
    InMemoryConfiguration inMemoryConfiguration;

    public SecSecurityMemoryConfig() {
        super();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        InMemoryUsersProperties users = inMemoryConfiguration.users();
        PasswordEncoder passwordEncoder = inMemoryConfiguration.passwordEncoder();
        users.getUsers().forEach(user -> {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.getProfiles().forEach(user::grantAuthority);
        });

        auth
            .userDetailsService(inMemoryConfiguration.userDetailsService())
            .passwordEncoder(passwordEncoder);
    }
}
