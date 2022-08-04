package com.chutneytesting.security;

import static java.util.Collections.singleton;

import com.chutneytesting.security.api.UserController;
import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.Authorizations;
import com.chutneytesting.server.core.security.UserRoles;
import com.chutneytesting.security.infra.handlers.Http401FailureHandler;
import com.chutneytesting.security.infra.handlers.HttpEmptyLogoutSuccessHandler;
import com.chutneytesting.security.infra.handlers.HttpStatusInvalidSessionStrategy;
import com.chutneytesting.security.infra.memory.InMemoryUserDetailsService;
import com.chutneytesting.security.infra.memory.InMemoryUsersProperties;
import com.chutneytesting.server.core.security.Authorization;
import com.chutneytesting.server.core.security.Role;
import com.chutneytesting.server.core.security.User;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
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
        http
            .authorizeRequests()
                .antMatchers(LOGIN_URL).permitAll()
                .antMatchers(LOGOUT_URL).permitAll()
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

        UserRoles userRoles = authorizations.read();
        Role role = userRoles.roleByName(Role.DEFAULT.name);
        anonymous.setRoles(singleton(role.name));
        role.authorizations.stream().map(Enum::name).forEach(anonymous::grantAuthority);

        if (role.authorizations.isEmpty()) {
            anonymous.grantAuthority("ANONYMOUS");
        }

        return anonymous;
    }

    @Configuration
    @Profile("mem-auth")
    public static class SecSecurityMemoryConfig {

        @Autowired
        protected void configure(
            final AuthenticationManagerBuilder auth,
            final InMemoryUsersProperties users,
            final PasswordEncoder passwordEncoder,
            final InMemoryUserDetailsService inMemoryUserDetailsService) throws Exception {

            users.getUsers().forEach(user ->
                user.setPassword(passwordEncoder.encode(user.getPassword()))
            );

            auth
                .userDetailsService(inMemoryUserDetailsService)
                .passwordEncoder(passwordEncoder);
        }
    }

    @Configuration
    @Profile({"ldap-auth", "ldap-auth-tls1-1"})
    public static class SecSecurityLDAPConfig {

        @Autowired
        protected void configure(
            final AuthenticationManagerBuilder auth,
            final LdapContextSource ldapContextSource,
            final UserDetailsContextMapper userDetailsContextMapper) throws Exception {

            auth
                .ldapAuthentication()
                .userSearchFilter("(uid={0})")
                .ldapAuthoritiesPopulator(new NullLdapAuthoritiesPopulator())
                .userDetailsContextMapper(userDetailsContextMapper)
                .contextSource(ldapContextSource)
                .rolePrefix("");
        }
    }
}
