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

package com.chutneytesting.server.core.domain.security;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

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
            .orElseThrow(() -> RoleNotFoundException.forRole(roleName));
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
                if (isBlank(user.roleName)) {
                    throw new IllegalArgumentException("Role declared for user [" + user.id + "] is blank");
                }
                Optional<Role> userRole = roles.stream()
                    .filter(Role.roleByNamePredicate(user.roleName))
                    .findFirst();
                if (userRole.isEmpty()) {
                    throw new IllegalArgumentException("Role [" + user.roleName + "] declared for user [" + user.id + "] is not defined");
                }
            }
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
