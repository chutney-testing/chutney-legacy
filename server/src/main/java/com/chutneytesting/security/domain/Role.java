package com.chutneytesting.security.domain;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Role {

    public static final Role DEFAULT = Role.builder().build();

    public final String name;
    public final Set<Authorization> authorizations;

    private Role(String name, Set<Authorization> authorizations) {
        this.name = name;
        this.authorizations = authorizations;
    }

    public static Predicate<Role> roleByNamePredicate(String roleName) {
        return role -> role.name.equals(roleName);
    }

    public static Role.RoleBuilder builder() {
        return new Role.RoleBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return name.equals(role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Role{" +
            "name='" + name + '\'' +
            ", authorizations=" + authorizations +
            '}';
    }

    public static class RoleBuilder {
        private static final Predicate<String> ROLE_NAME_PREDICATE = Pattern.compile("^[0-9a-zA-Z_]+$").asMatchPredicate();

        private String name;
        private Set<String> authorizations;

        private RoleBuilder() {
        }

        public Role build() {
            return new Role(
                validateRoleName(),
                validateAuthorizations(authorizations)
            );
        }

        public RoleBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public RoleBuilder withAuthorizations(Collection<String> authorizations) {
            if (ofNullable(authorizations).isPresent()) {
                this.authorizations = new LinkedHashSet<>(authorizations);
            }
            return this;
        }

        private String validateRoleName() {
            String n = ofNullable(name).orElse("DEFAULT");
            if (!ROLE_NAME_PREDICATE.test(n)) {
                throw new IllegalArgumentException("Role name must be alphanumeric with underscores, i.e. must match the pattern `[0-9a-zA-Z_]+`");
            }
            return n;
        }

        private Set<Authorization> validateAuthorizations(Set<String> authorizations) {
            return ofNullable(authorizations).orElse(emptySet()).stream()
                .map(Authorization::valueOf)
                .collect(toCollection(LinkedHashSet::new));
        }
    }
}
