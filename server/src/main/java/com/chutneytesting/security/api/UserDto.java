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

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class UserDto implements UserDetails {

    private String id;
    private String name;
    private String firstname;
    private String lastname;
    private String mail;

    @JsonIgnore
    private Set<String> roles;
    private Set<GrantedAuthority> authorizations;
    @JsonIgnore // For in-memory usage
    private String password;

    public UserDto() {
    }

    public UserDto(UserDto copyFrom) {
        this.id = copyFrom.getId();
        this.name = copyFrom.getName();
        this.firstname = copyFrom.getFirstname();
        this.lastname = copyFrom.getLastname();
        this.mail = copyFrom.getMail();
        this.roles = new HashSet<>(copyFrom.getRoles());
        this.authorizations = new HashSet<>(copyFrom.getAuthorities());
        this.password = copyFrom.getPassword();
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return ofNullable(authorizations).orElse(emptySet());
    }

    public Collection<String> getAuthorizations() {
        return ofNullable(authorizations)
            .map(a -> a.stream().map(GrantedAuthority::getAuthority).collect(toSet()))
            .orElse(emptySet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return id;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Set<String> getRoles() {
        return ofNullable(roles).orElse(emptySet());
    }

    public void setRoles(Set<String> roles) {
        roles.forEach(this::addRole);
    }

    public void addRole(String role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }

    public void grantAuthority(String authority) {
        if (authorizations == null) {
            authorizations = new HashSet<>();
        }
        authorizations.add(new SimpleGrantedAuthority(authority));
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
