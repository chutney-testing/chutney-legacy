package com.chutneytesting.server.core.domain.security;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class UserRoles {

    private final Map<Role, Set<User>> userRoleMap;

    private UserRoles(Map<Role, Set<User>> userRoleMap) {
        this.userRoleMap = userRoleMap;
    }

    public Optional<User> userById(String userId) {
        return users().stream()
            .filter(User.userByIdPredicate(userId))
            .findFirst();
    }

    public Role roleByName(String roleName) {
        return roles().stream()
            .filter(Role.roleByNamePredicate(roleName))
            .findFirst()
            .orElseThrow(() -> new RoleNotFoundException("Role [" + roleName + "] is not defined"));
    }

    public User addNewUser(String id) {
        Objects.requireNonNull(id);

        User newUser = User.builder()
            .withId(id)
            .withRole(Role.DEFAULT.name)
            .build();

        this.userRoleMap.get(Role.DEFAULT).add(newUser);
        return newUser;
    }

    public Set<Role> roles() {
        return this.userRoleMap.keySet();
    }

    public Set<User> users() {
        return this.userRoleMap.values().stream()
            .flatMap(Collection::stream)
            .collect(toCollection(LinkedHashSet::new));
    }

    public Set<User> usersByRole(Role role) {
        Objects.requireNonNull(role);

        return users().stream()
            .filter(User.userByRoleNamePredicate(role.name))
            .collect(toCollection(LinkedHashSet::new));
    }

    public static UserRolesBuilder builder() {
        return new UserRolesBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoles userRoles = (UserRoles) o;
        return userRoleMap.equals(userRoles.userRoleMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userRoleMap);
    }

    @Override
    public String toString() {
        return "UserRoles{" +
            "userRoleMap=" + userRoleMap +
            '}';
    }

    public static class UserRolesBuilder {

        private Set<Role> roles = emptySet();
        private Set<User> users = emptySet();

        private UserRolesBuilder() {
        }

        public UserRoles build() {
            checkDefaultRole();
            checkUserRoles();

            return new UserRoles(
                roles.stream().collect(toMap(
                    r -> r,
                    r -> users.stream().filter(User.userByRoleNamePredicate(r.name)).collect(toCollection(LinkedHashSet::new)),
                    (x, y) -> y,
                    LinkedHashMap::new
                ))
            );
        }

        private void checkUserRoles() {
            for (User user : users) {
                Optional<Role> userRole = roles.stream()
                    .filter(Role.roleByNamePredicate(user.roleName))
                    .findFirst();
                if (userRole.isEmpty()) {
                    throw new IllegalArgumentException("Role [" + user.roleName + "] declared for user [" + user.id + "] is not defined");
                }
            }
        }

        private void checkDefaultRole() {
            if (roles == null || roles.isEmpty()) {
                roles = new LinkedHashSet<>();
                roles.add(Role.DEFAULT);
            }

            roles.stream()
                .filter(Role.DEFAULT::equals)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role [" + Role.DEFAULT.name + "] must be defined"));
        }

        public UserRolesBuilder withRoles(Collection<Role> roles) {
            if (ofNullable(roles).isPresent()) {
                this.roles = new LinkedHashSet<>(roles);
            }
            return this;
        }

        public UserRolesBuilder withUsers(Collection<User> users) {
            if (ofNullable(users).isPresent()) {
                this.users = new LinkedHashSet<>(users);
            }
            return this;
        }
    }
}
