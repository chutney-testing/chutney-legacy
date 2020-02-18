package com.chutneytesting.security.infra.ldap;

import java.util.Collection;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class LdapUserDetailsContextMapper implements UserDetailsContextMapper {

    private UserDetailsService userDetailsService;

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
