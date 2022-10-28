package com.chutneytesting.security.infra.ldap;

import static java.util.Optional.ofNullable;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
import com.chutneytesting.security.infra.UserDetailsServiceHelper;
import java.util.List;
import javax.naming.directory.SearchControls;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class LdapUserDetailsService implements UserDetailsService {

    private final String userIdAttribute;
    private final LdapTemplate ldapTemplate;
    private final AttributesMapper<UserDto> ldapAttributesMapper;
    private final String[] attributesToRetrieve;
    private final AuthenticationService authenticationService;
    private int userSearchScope;

    LdapUserDetailsService(
        LdapTemplate ldapTemplate,
        LdapAttributesProperties ldapAttributesProperties,
        AttributesMapper<UserDto> ldapAttributesMapper,
        AuthenticationService authenticationService,
        String userSearchScope
    ) {
        this.userIdAttribute = ldapAttributesProperties.getId();
        this.ldapTemplate = ldapTemplate;
        this.ldapAttributesMapper = ldapAttributesMapper;
        this.attributesToRetrieve = ldapAttributesProperties.attributes();
        this.authenticationService = authenticationService;
        this.userSearchScope = initSearchScope(userSearchScope);
    }

    private int initSearchScope(String userSearchScope) {
        switch (ofNullable(userSearchScope).map(String::toUpperCase).orElse("")) {
            case "SUBTREE":
                return SearchControls.SUBTREE_SCOPE;
            case "ONELEVEL":
            default:
                return SearchControls.ONELEVEL_SCOPE;

        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        final List<UserDto> results;
        try {
            results = ldapTemplate.search("", new EqualsFilter(userIdAttribute, username).encode(), userSearchScope, attributesToRetrieve, ldapAttributesMapper);

            if (results.size() != 1) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException("Cannot retrieve ldap attributes for user: " + username);
        }

        return UserDetailsServiceHelper.grantAuthoritiesFromUserRole(results.get(0), authenticationService);
    }
}
