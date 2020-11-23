package com.chutneytesting.security;

import com.chutneytesting.security.infra.handlers.Http401FailureHandler;
import com.chutneytesting.security.infra.handlers.HttpEmptyLogoutSuccessHandler;
import com.chutneytesting.security.infra.handlers.HttpStatusInvalidSessionStrategy;
import com.chutneytesting.security.infra.ldap.LdapConfiguration;
import com.chutneytesting.security.infra.memory.InMemoryConfiguration;
import com.chutneytesting.security.infra.memory.InMemoryUsersProperties;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class ChutneySecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        configureBaseHttpSecurity(http);
        http
            .anonymous()
                .disable()
            .authorizeRequests()
                .antMatchers("/api/v1/user/login").permitAll()
                .antMatchers("/api/v1/user/logout").permitAll()
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            .and()
            .httpBasic();
    }

    protected void configureBaseHttpSecurity(final HttpSecurity http) throws Exception {
        Map<String, String> invalidSessionHeaders = new HashMap<>();
        invalidSessionHeaders.put(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        http
            .sessionManagement()
                .invalidSessionStrategy(new HttpStatusInvalidSessionStrategy(HttpStatus.UNAUTHORIZED, invalidSessionHeaders))
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
                .loginProcessingUrl("/api/v1/user/login")
                .successForwardUrl("/api/v1/user")
                .failureHandler(new Http401FailureHandler())
            .and()
            .logout()
                .logoutUrl("/api/v1/user/logout")
                .logoutSuccessHandler(new HttpEmptyLogoutSuccessHandler());
    }

    @Configuration
    @Profile("mem-auth")
    public static class SecSecurityMemoryConfig {

        @Autowired
        InMemoryConfiguration inMemoryConfiguration;

        @Autowired
        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
            InMemoryUsersProperties users = inMemoryConfiguration.users();
            PasswordEncoder passwordEncoder = inMemoryConfiguration.passwordEncoder();
            users.getUsers().forEach(user -> {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.getProfiles().forEach(user::grantAuthority);
            });

            auth
                .userDetailsService(inMemoryConfiguration.inMemoryUserDetailsService())
                .passwordEncoder(passwordEncoder);
        }
    }

    @Configuration
    @Profile("ldap-auth")
    public static class SecSecurityLDAPConfig {

        @Autowired
        LdapConfiguration ldapConfiguration;

        @Autowired
        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
            auth
                .ldapAuthentication()
                .userSearchFilter("(uid={0})")
                .ldapAuthoritiesPopulator(new NullLdapAuthoritiesPopulator())
                .userDetailsContextMapper(ldapConfiguration.userDetailsContextMapper(null))
                .contextSource(ldapConfiguration.contextSource());
        }
    }
}
