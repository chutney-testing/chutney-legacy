package com.chutneytesting.security;

import com.chutneytesting.security.infra.handlers.Http401FailureHandler;
import com.chutneytesting.security.infra.handlers.HttpEmptyLogoutSuccessHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

public abstract class SecSecurityUILoginAbstractConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .csrf()
                .disable()
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
            .authorizeRequests()
                .anyRequest().permitAll();
    }

}
