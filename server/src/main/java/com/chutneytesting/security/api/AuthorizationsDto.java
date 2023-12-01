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

package com.chutneytesting.security.api;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

public class AuthorizationsDto {
    @JsonProperty
    private List<RoleDto> roles = emptyList();
    @JsonProperty
    private List<RoleUsersDto> authorizations = emptyList();

    public List<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }

    public List<RoleUsersDto> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(List<RoleUsersDto> authorizations) {
        this.authorizations = authorizations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizationsDto that = (AuthorizationsDto) o;
        return Objects.equals(roles, that.roles) && Objects.equals(authorizations, that.authorizations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roles, authorizations);
    }

    @Override
    public String toString() {
        return "AuthorizationsDto{" +
            "roles=" + roles +
            ", authorizations=" + authorizations +
            '}';
    }

    public static class RoleDto {
        @JsonProperty
        private String name = "";
        @JsonProperty
        private List<String> rights = emptyList();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getRights() {
            return rights;
        }

        public void setRights(List<String> rights) {
            this.rights = rights;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RoleDto roleDto = (RoleDto) o;
            return Objects.equals(name, roleDto.name) && Objects.equals(rights, roleDto.rights);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, rights);
        }

        @Override
        public String toString() {
            return "RoleDto{" +
                "name='" + name + '\'' +
                ", rights=" + rights +
                '}';
        }
    }

    public static class RoleUsersDto {
        @JsonProperty
        private String name = "";
        @JsonProperty
        private List<String> users = emptyList();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getUsers() {
            return users;
        }

        public void setUsers(List<String> users) {
            this.users = users;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RoleUsersDto that = (RoleUsersDto) o;
            return Objects.equals(name, that.name) && Objects.equals(users, that.users);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, users);
        }

        @Override
        public String toString() {
            return "RoleUsersDto{" +
                "name='" + name + '\'' +
                ", users=" + users +
                '}';
        }
    }
}
