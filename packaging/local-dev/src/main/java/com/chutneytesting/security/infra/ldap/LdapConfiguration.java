/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.security.infra.ldap;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
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
    public AttributesMapper<UserDto> attributesMapper(
        LdapAttributesProperties ldapAttributesProperties,
        @Value("${ldap.groups-pattern}") String ldapGroupsPattern
    ) {
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
