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

