package com.chutneytesting;

import com.chutneytesting.security.domain.User;
import com.chutneytesting.security.infra.handlers.Http401FailureHandler;
import com.chutneytesting.security.infra.handlers.HttpEmptyLogoutSuccessHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
@Profile("dev-auth")
public class SecSecurityMemoryConfig extends com.chutneytesting.security.SecSecurityMemoryConfig {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .csrf()
            .disable()
            .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            .and()
            .requiresChannel()
                .anyRequest().requiresSecure()
            .and()
                .formLogin()
                    .loginProcessingUrl("/api/v1/user/login")
                    .successForwardUrl("/api/v1/user")
                    .failureHandler(new Http401FailureHandler())
            .and()
                .logout()
                    .logoutUrl("/api/v1/user/logout")
                    .logoutSuccessHandler(new HttpEmptyLogoutSuccessHandler())
            .and()
            .anonymous()
                .principal(anonymousChutneyUser())
            .and()
            .authorizeRequests()
                .anyRequest().permitAll()
            .and()
            .httpBasic();
    }

    private User anonymousChutneyUser() {
        User user = new User();
        user.setId("guest");
        user.setName("guest");
        return user;
    }
}
