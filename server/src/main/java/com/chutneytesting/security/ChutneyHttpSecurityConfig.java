package com.chutneytesting.security;

import com.chutneytesting.admin.api.InfoController;
import com.chutneytesting.security.api.UserController;
import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.Authorizations;
import com.chutneytesting.security.infra.handlers.Http401FailureHandler;
import com.chutneytesting.security.infra.handlers.HttpEmptyLogoutSuccessHandler;
import com.chutneytesting.security.infra.handlers.HttpStatusInvalidSessionStrategy;
import com.chutneytesting.server.core.domain.security.Authorization;
import com.chutneytesting.server.core.domain.security.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class ChutneyHttpSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String LOGIN_URL = UserController.BASE_URL + "/login";
    public static final String LOGOUT_URL = UserController.BASE_URL + "/logout";
    public static final String API_BASE_URL_PATTERN = "/api/**";
    public static final String ACTUATOR_BASE_URL_PATTERN = "/actuator/**";

    @Value("${server.servlet.session.cookie.http-only:true}")
    private boolean sessionCookieHttpOnly;
    @Value("${server.servlet.session.cookie.secure:true}")
    private boolean sessionCookieSecure;

    @Autowired
    private Authorizations authorizations;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        configureBaseHttpSecurity(http);
        UserDto anonymous = anonymous();
        http
            .anonymous()
                .principal(anonymous)
                .authorities(new ArrayList<>(anonymous.getAuthorities()))
            .and()
            .authorizeRequests()
                .antMatchers(LOGIN_URL).permitAll()
                .antMatchers(LOGOUT_URL).permitAll()
                .antMatchers(InfoController.BASE_URL + "/**").permitAll()
                .antMatchers(API_BASE_URL_PATTERN).authenticated()
                .antMatchers(ACTUATOR_BASE_URL_PATTERN).hasAuthority(Authorization.ADMIN_ACCESS.name())
            .anyRequest().permitAll()
            .and()
            .httpBasic();
    }

    protected void configureBaseHttpSecurity(final HttpSecurity http) throws Exception {
        Map<String, String> invalidSessionHeaders = new HashMap<>();
        invalidSessionHeaders.put(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        http
            .sessionManagement()
                .invalidSessionStrategy(new HttpStatusInvalidSessionStrategy(HttpStatus.UNAUTHORIZED, invalidSessionHeaders, sessionCookieHttpOnly, sessionCookieSecure))
            .and()
            .csrf()
                .disable()
            .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            .and()
            .requiresChannel()
                .anyRequest().requiresSecure()
            .and()
            .formLogin()
                .loginProcessingUrl(LOGIN_URL)
                .successForwardUrl(UserController.BASE_URL)
                .failureHandler(new Http401FailureHandler())
            .and()
            .logout()
                .logoutUrl(LOGOUT_URL)
                .logoutSuccessHandler(new HttpEmptyLogoutSuccessHandler());
    }

    protected UserDto anonymous() {
        UserDto anonymous = new UserDto();
        anonymous.setId(User.ANONYMOUS.id);
        anonymous.setName(User.ANONYMOUS.id);
        anonymous.grantAuthority("ANONYMOUS");
        return anonymous;
    }
}
