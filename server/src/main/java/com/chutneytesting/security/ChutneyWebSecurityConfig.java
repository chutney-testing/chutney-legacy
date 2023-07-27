package com.chutneytesting.security;

import com.chutneytesting.admin.api.InfoController;
import com.chutneytesting.security.api.UserController;
import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
import com.chutneytesting.security.domain.Authorizations;
import com.chutneytesting.security.infra.handlers.Http401FailureHandler;
import com.chutneytesting.security.infra.handlers.HttpEmptyLogoutSuccessHandler;
import com.chutneytesting.server.core.domain.security.Authorization;
import com.chutneytesting.server.core.domain.security.User;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ChutneyWebSecurityConfig {

    public static final String LOGIN_URL = UserController.BASE_URL + "/login";
    public static final String LOGOUT_URL = UserController.BASE_URL + "/logout";
    public static final String API_BASE_URL_PATTERN = "/api/**";

    @Value("${management.endpoints.web.base-path:'/actuator'}")
    String actuatorBaseUrl;

    @Bean
    public AuthenticationService authenticationService(Authorizations authorizations) {
        return new AuthenticationService(authorizations);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        configureBaseHttpSecurity(http);
        UserDto anonymous = anonymous();
        return http.anonymous(anonymousConfigurer -> anonymousConfigurer
                .authorities(new ArrayList<>(anonymous.getAuthorities())))
            .authorizeHttpRequests(httpRequest -> httpRequest
                .requestMatchers(LOGIN_URL).permitAll()
                .requestMatchers(LOGOUT_URL).permitAll()
                .requestMatchers(InfoController.BASE_URL + "/**").permitAll()
                .requestMatchers(API_BASE_URL_PATTERN).authenticated()
                .requestMatchers(actuatorBaseUrl + "/**").hasAuthority(Authorization.ADMIN_ACCESS.name())
                .anyRequest().permitAll())
            .httpBasic(Customizer.withDefaults()).build();
    }

    protected void configureBaseHttpSecurity(final HttpSecurity http) throws Exception {
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
