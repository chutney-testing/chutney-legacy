package com.chutneytesting.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class User implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    private String id;
    private String name;
    private String firstname;
    private String lastname;
    private String mail;

    @JsonIgnore
    private Set<String> profiles;
    private Set<GrantedAuthority> authorities;

    // For in-memory usage
    @JsonIgnore
    private String password;
    @JsonIgnore
    private boolean accountExpired = false;
    @JsonIgnore
    private boolean locked = false;
    @JsonIgnore
    private boolean credentialExpired = false;
    @JsonIgnore
    private boolean enabled = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return !accountExpired;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return !credentialExpired;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return !enabled;
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

    public Set<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<String> profiles) {
        this.profiles = profiles;
    }

    public void addProfile(String profile) {
        if (profiles == null) {
            profiles = new HashSet<>();
        }
        profiles.add(profile);
    }

    public void grantAuthority(String authority) {
        if (authorities == null) {
            authorities = new HashSet<>();
        }
        authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + authority));
    }

    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setCredentialExpired(boolean credentialExpired) {
        this.credentialExpired = credentialExpired;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
