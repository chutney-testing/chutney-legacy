package com.chutneytesting.security.api;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;

import com.chutneytesting.security.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDto implements UserDetails {

    public static UserDto ANONYMOUS;

    static {
        ANONYMOUS = new UserDto();
        ANONYMOUS.setId(User.ANONYMOUS.id);
        ANONYMOUS.setName(User.ANONYMOUS.id);
        ANONYMOUS.setRoles(singleton(User.ANONYMOUS.roleName));
    }

    private String id;
    private String name;
    private String firstname;
    private String lastname;
    private String mail;

    @JsonIgnore
    private Set<String> roles;
    private Set<GrantedAuthority> authorizations;

    // For in-memory usage
    @JsonIgnore
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return ofNullable(authorizations).orElse(emptySet());
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
