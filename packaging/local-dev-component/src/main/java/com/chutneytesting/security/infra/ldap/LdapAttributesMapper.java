package com.chutneytesting.security.infra.ldap;

import static java.util.Optional.ofNullable;

import com.chutneytesting.security.api.UserDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;

public class LdapAttributesMapper implements AttributesMapper<UserDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAttributesMapper.class);
    private final Pattern ldapGroupPattern;
    private final LdapAttributesProperties ldapAttributesProperties;

    LdapAttributesMapper(LdapAttributesProperties ldapAttributesProperties,
                         String ldapGroupsPattern) {
        this.ldapGroupPattern = Pattern.compile(ldapGroupsPattern);
        this.ldapAttributesProperties = ldapAttributesProperties;
    }

    @Override
    public UserDto mapFromAttributes(Attributes attributes) throws NamingException {
        UserDto user = new UserDto();

        consumeLDAPAttribute(ldapAttributesProperties.getId(), attributes, user::setId);
        consumeLDAPAttribute(ldapAttributesProperties.getName(), attributes, user::setName);
        consumeLDAPAttribute(ldapAttributesProperties.getFirstname(), attributes, user::setFirstname);
        consumeLDAPAttribute(ldapAttributesProperties.getLastname(), attributes, user::setLastname);
        consumeLDAPAttribute(ldapAttributesProperties.getMail(), attributes, user::setMail);

        ofNullable(ldapAttributesProperties.getGroups()).ifPresent(groupProperty -> {
            try {
                List<String> groups = extractAttributeMultiValue(attributes.get(groupProperty));
                groups.stream()
                    .map(this::applyLdapGroupMatcher)
                    .filter(Objects::nonNull)
                    .forEach(user::addRole);
            } catch (NamingException e) {
                LOGGER.warn("Cannot retrieve groups from LDAP", e);
            }
        });

        return user;
    }

    private void consumeLDAPAttribute(String ldapAttributeName, Attributes attributes, Consumer<String> setter) {
        ofNullable(ldapAttributeName).ifPresent(ldapAttr -> {
            try {
                setter.accept(extractAttributeMonoValue(attributes.get(ldapAttr)));
            } catch (NamingException e) {
                LOGGER.warn("Cannot retrieve {} from LDAP", ldapAttr, e);
            }
        });
    }

    private String applyLdapGroupMatcher(String ldapGroup) {
        Matcher ldapGroupMatcher = ldapGroupPattern.matcher(ldapGroup);
        if (ldapGroupMatcher.matches()) {
            return ldapGroupMatcher.group(1);
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
            while (nameValues.hasMoreElements()) {
                T v = nameValues.nextElement();
                if (v != null) {
                    values.add(v);
                }
            }
        }
        return values;
    }
}
