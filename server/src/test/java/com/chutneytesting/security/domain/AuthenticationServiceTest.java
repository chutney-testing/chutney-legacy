package com.chutneytesting.security.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthenticationServiceTest {

    private final Authorizations authorizations = mock(Authorizations.class);
    private AuthenticationService sut;

    @BeforeEach
    public void setUp() {
        sut = new AuthenticationService(authorizations);
    }

    @Test
    public void should_get_role_from_user_id() {
        // Given
        Role expectedRole = Role.builder()
            .withName("roleName")
            .withAuthorizations(List.of(Authorization.SCENARIO_EXECUTE.name(), Authorization.COMPONENT_READ.name()))
            .build();
        when(authorizations.read()).thenReturn(
            UserRoles.builder()
                .withRoles(List.of(Role.DEFAULT, expectedRole))
                .withUsers(List.of(User.builder().withId("userId").withRole(expectedRole.name).build()))
                .build()
        );

        // When
        Role role = sut.userRoleById("userId");

        // Then
        assertThat(role).isEqualTo(expectedRole);
        assertThat(role.authorizations).containsExactlyInAnyOrderElementsOf(expectedRole.authorizations);
    }

    @Test
    public void should_add_unknown_user_with_default_role_when_get_role_for_authentication() {
        // Given
        when(authorizations.read()).thenReturn(
            UserRoles.builder().build()
        );

        // When
        Role role = sut.userRoleById("unknown-user");

        // Then
        assertThat(role).isEqualTo(Role.DEFAULT);
        verify(authorizations).save(any());
    }
}
