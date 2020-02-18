package com.chutneytesting.security;

import com.chutneytesting.security.infra.ldap.LdapConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator;

@Configuration
@EnableWebSecurity
@Profile("ldap-auth")
public class SecSecurityLDAPConfig extends SecSecurityUILoginAbstractConfig {

    @Autowired
    LdapConfiguration ldapConfiguration;

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth
            .ldapAuthentication()
            .userSearchFilter("(uid={0})")
            .ldapAuthoritiesPopulator(new NullLdapAuthoritiesPopulator())
            .userDetailsContextMapper(ldapConfiguration.userDetailsContextMapper(null))
            .contextSource(ldapConfiguration.contextSource());
    }
}
