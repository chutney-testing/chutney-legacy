package com.chutneytesting.security.infra.ldap;

import com.chutneytesting.security.domain.User;
import com.chutneytesting.security.domain.UserRoles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import org.springframework.ldap.core.AttributesMapper;

public class LdapAttributesMapper implements AttributesMapper<User> {

    private final Pattern ldapGroupPattern;
    private final LdapAttributesProperties ldapAttributesProperties;

    LdapAttributesMapper(LdapAttributesProperties ldapAttributesProperties, String ldapGroupsPattern) {
        this.ldapGroupPattern = Pattern.compile(ldapGroupsPattern);
        this.ldapAttributesProperties = ldapAttributesProperties;
    }

    @Override
    public User mapFromAttributes(Attributes attributes) throws NamingException {
        User user = new User();

        user.setId(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getId())));
        user.setName(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getName())));
        user.setFirstname(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getFirstname())));
        user.setLastname(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getLastname())));
        user.setMail(extractAttributeMonoValue(attributes.get(ldapAttributesProperties.getMail())));

        List<String> groups = extractAttributeMultiValue(attributes.get(ldapAttributesProperties.getGroups()));
        groups.stream()
            .map(this::mapLdapGroupToProfile)
            .filter(Objects::nonNull)
            .forEach(s -> updateUserProfiles(user, s));

        if (user.getProfiles() == null || user.getProfiles().isEmpty()) {
            user.grantAuthority(UserRoles.ANONYMOUS);
        }

        return user;
    }

    private void updateUserProfiles(User user, Profiles profile) {
        user.addProfile(profile.name());
        user.grantAuthority(profile.name());
    }

    private Profiles mapLdapGroupToProfile(String ldapGroup) {
        Matcher ldapGroupMatcher = ldapGroupPattern.matcher(ldapGroup);
        if (ldapGroupMatcher.matches()) {
            return Profiles.valueOf(ldapGroupMatcher.group(1));
        }
        return null;
    }

    private String extractAttributeMonoValue(Attribute attribute) throws NamingException {
        String value = null;

        if (attribute != null) {
            Object attrValue = attribute.get();
            if (attrValue instanceof String) {
                value = (String) attrValue;
            } else {
                value = Objects.toString(attrValue);
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> extractAttributeMultiValue(Attribute attribute) throws NamingException {
        List<T> values = new ArrayList<>();

        if (attribute != null) {
            NamingEnumeration<T> nameValues = (NamingEnumeration<T>) attribute.getAll();
            while(nameValues.hasMoreElements()) {
                T v = nameValues.nextElement();
                if (v != null) {
                    values.add(v);
                }
            }
        }
        return values;
    }

    private enum Profiles {
        UTILISATEUR, ADMINISTRATEUR
    }
}
