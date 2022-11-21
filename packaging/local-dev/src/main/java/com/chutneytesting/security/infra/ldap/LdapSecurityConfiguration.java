package com.chutneytesting.security.infra.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

@Configuration
@Profile({"ldap-auth", "ldap-auth-tls1-1"})
public class LdapSecurityConfiguration {

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

