package com.chutneytesting.security;

import com.chutneytesting.security.infra.handlers.Http401FailureHandler;
import com.chutneytesting.security.infra.handlers.HttpEmptyLogoutSuccessHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

public abstract class SecSecurityUILoginAbstractConfig extends WebSecurityConfigurerAdapter {

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
            .authorizeRequests()
                .antMatchers("/api/v1/user/login").permitAll()
                .antMatchers("/api/v1/user/logout").permitAll()
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll();
    }

}
