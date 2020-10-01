package com.chutneytesting.security.infra.ldap;

import com.chutneytesting.security.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

@Configuration
@Profile("ldap-auth")
public class LdapConfiguration {

    @Bean
    @ConfigurationProperties("ldap.source")
    public LdapContextSource contextSource() {
        return new LdapContextSource();
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
    public AttributesMapper<User> attributesMapper(LdapAttributesProperties ldapAttributesProperties, @Value("${ldap.groups-pattern}") String ldapGroupsPattern) {
        return new LdapAttributesMapper(ldapAttributesProperties, ldapGroupsPattern);
    }

    @Bean
    public LdapUserDetailsService ldapUserDetailsService(LdapTemplate ldapTemplate, LdapAttributesProperties ldapAttributesProperties, AttributesMapper<User> attributesMapper) {
        return new LdapUserDetailsService(ldapTemplate, ldapAttributesProperties, attributesMapper);
    }

    @Bean
    public UserDetailsContextMapper userDetailsContextMapper(LdapUserDetailsService ldapUserDetailsService) {
        return new LdapUserDetailsContextMapper(ldapUserDetailsService);
    }
}
