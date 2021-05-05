package com.chutneytesting.security.infra.ldap;

import com.chutneytesting.security.domain.User;
import java.util.List;
import javax.naming.directory.SearchControls;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class LdapUserDetailsService implements UserDetailsService {

    private final String userIdAttribute;
    private final LdapTemplate ldapTemplate;
    private final AttributesMapper<User> ldapAttributesMapper;
    private final String[] attributesToRetrieve;

    LdapUserDetailsService(LdapTemplate ldapTemplate, LdapAttributesProperties ldapAttributesProperties, AttributesMapper<User> ldapAttributesMapper) {
        this.userIdAttribute = ldapAttributesProperties.getId();
        this.ldapTemplate = ldapTemplate;
        this.ldapAttributesMapper = ldapAttributesMapper;
        this.attributesToRetrieve = ldapAttributesProperties.attributes();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        final List<User> results = ldapTemplate.search("", new EqualsFilter(userIdAttribute, username).encode(), SearchControls.ONELEVEL_SCOPE, attributesToRetrieve, ldapAttributesMapper);

        if (results.size() != 1) {
            throw new AuthenticationException();
        }

        return results.get(0);
    }
}
