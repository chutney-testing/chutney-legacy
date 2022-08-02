package com.chutneytesting.security;

import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class User {

    public static User ANONYMOUS = User.builder().build();

    public final String id;
    public final String roleName;

    private User(String id, String roleName) {
        this.id = id;
        this.roleName = roleName;
    }

    public static boolean isAnonymous(String userId) {
        return ANONYMOUS.id.equals(userId);
    }

    public static Predicate<User> userByRoleNamePredicate(String roleName) {
        return user -> user.roleName.equals(roleName);
    }

    public static Predicate<User> userByIdPredicate(String userId) {
        return user -> user.id.equals(userId);
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
            "id='" + id + '\'' +
            ", role=" + roleName +
            '}';
    }

    public static class UserBuilder {
        private static final Predicate<String> USER_NAME_PREDICATE = Pattern.compile("^[0-9a-zA-Z_-]+$").asMatchPredicate();
        private static final String ANONYMOUS_USER_ID = "guest";

        private String id;
        private String role;

        private UserBuilder() {
        }

        public User build() {
            return new User(
                validateUserId(),
                ofNullable(role).orElse(Role.DEFAULT.name)
            );
        }

        public UserBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public UserBuilder withRole(String role) {
            this.role = role;
            return this;
        }

        private String validateUserId() {
            if (ofNullable(id).isEmpty()) {
                return ANONYMOUS_USER_ID;
            }
            if (!USER_NAME_PREDICATE.test(id)) {
                throw new IllegalArgumentException("User id must match the pattern `[0-9a-zA-Z_-]+`");
            }
            if (ANONYMOUS_USER_ID.equals(id)) {
                throw new IllegalArgumentException("User id [" + ANONYMOUS_USER_ID + "] cannot be used");
            }
            return id;
        }
    }
}
