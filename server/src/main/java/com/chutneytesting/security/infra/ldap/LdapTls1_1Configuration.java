package com.chutneytesting.security.infra.ldap;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
import com.chutneytesting.security.infra.ldap.ssl.ChutneyDirContextAuthenticationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

@Configuration
@Profile("ldap-auth-tls1-1")
public class LdapTls1_1Configuration {

    @Bean
    @ConfigurationProperties("ldap.source")
    public LdapContextSource contextSource(DirContextAuthenticationStrategy strategy) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setAuthenticationStrategy(strategy);
        return contextSource;
    }

    @Bean
    public DirContextAuthenticationStrategy strategy() {
        return new ChutneyDirContextAuthenticationStrategy();
    }

    @Bean
    @ConfigurationProperties("ldap.attributes")
    public LdapAttributesProperties ldapAttributesProperties() {
        return new LdapAttributesProperties();
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    @Bean
    public AttributesMapper<UserDto> attributesMapper(LdapAttributesProperties ldapAttributesProperties,
                                                      @Value("${ldap.groups-pattern}") String ldapGroupsPattern) {
        return new LdapAttributesMapper(ldapAttributesProperties, ldapGroupsPattern);
    }

    @Bean
    public LdapUserDetailsService ldapUserDetailsService(
        LdapTemplate ldapTemplate,
        LdapAttributesProperties ldapAttributesProperties,
        AttributesMapper<UserDto> attributesMapper,
        AuthenticationService authenticationService,
        @Value("${ldap.user-search-scope}") String userSearchScope
    ) {
        return new LdapUserDetailsService(ldapTemplate, ldapAttributesProperties, attributesMapper, authenticationService, userSearchScope);
    }

    @Bean
    public UserDetailsContextMapper userDetailsContextMapper(LdapUserDetailsService ldapUserDetailsService) {
        return new LdapUserDetailsContextMapper(ldapUserDetailsService);
    }
}
