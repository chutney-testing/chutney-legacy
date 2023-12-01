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

import java.util.Collection;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class LdapUserDetailsContextMapper implements UserDetailsContextMapper {

    private final UserDetailsService userDetailsService;

    public LdapUserDetailsContextMapper(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        return userDetailsService.loadUserByUsername(username);
    }

    /**
     * From implementation {@link org.springframework.security.ldap.userdetails.LdapUserDetailsMapper#mapUserToContext(UserDetails, DirContextAdapter)}
     */
    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException(
            "LdapUserDetailsMapper only supports reading from a context. Please"
                + "use a subclass if mapUserToContext() is required.");
    }
}
