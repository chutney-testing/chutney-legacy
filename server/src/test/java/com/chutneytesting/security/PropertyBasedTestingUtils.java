package com.chutneytesting.security;

import static java.util.stream.Collectors.toSet;

import com.chutneytesting.server.core.domain.security.Authorization;
import com.chutneytesting.server.core.domain.security.Role;
import com.chutneytesting.server.core.domain.security.User;
import com.chutneytesting.server.core.domain.security.UserRoles;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.arbitraries.SetArbitrary;

public final class PropertyBasedTestingUtils {


    public static Arbitrary<UserRoles> validUserRoles() {
        SetArbitrary<Role> roles = validRole().set().ofMinSize(1).ofMaxSize(10);
        return roles.map(r -> {
            return UserRoles.builder()
                .withRoles(r)
                .withUsers(validUsers(r))
                .build();
        });
    }

    public static Arbitrary<Role> validRole() {
        return Combinators.combine(validRoleName(), validRights())
            .as((n, a) ->
                Role.builder()
                    .withName(n)
                    .withAuthorizations(a)
                    .build()
            );
    }

    public static Set<User> validUsers(Set<Role> roles) {
        Random rand = new Random();
        List<String> users = validUserId().list().uniqueElements().ofMaxSize(50).sample();
        return users.stream()
            .map(id -> User.builder()
                .withId(id)
                .withRole(randomRole(roles, rand))
                .build()
            ).collect(toSet());
    }

    public static Arbitrary<String> validRoleName() {
        return Arbitraries.strings()
            .alpha().numeric().withChars('_')
            .ofMinLength(1).ofMaxLength(20);
    }

    public static Arbitrary<String> validUserId() {
        return Arbitraries.strings()
            .alpha().numeric().withChars("_-")
            .ofMinLength(1).ofMaxLength(10);
    }

    public static SetArbitrary<String> validRights() {
        return Arbitraries.of(Authorization.class).map(Enum::name).set().ofMinSize(1).ofMaxSize(5);
    }

    public static String randomRole(Set<Role> roles, Random rand) {
        return roles.stream().skip(rand.nextInt(roles.size())).findFirst().map(r -> r.name).orElse(null);
    }
}
