package com.chutneytesting.security.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.server.core.domain.security.Authorization;
import com.chutneytesting.server.core.domain.security.Role;
import com.chutneytesting.server.core.domain.security.User;
import com.chutneytesting.server.core.domain.security.UserRoles;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
            .withName("expectedRole")
            .withAuthorizations(List.of(Authorization.SCENARIO_EXECUTE.name(), Authorization.COMPONENT_READ.name()))
            .build();
        when(authorizations.read()).thenReturn(
            UserRoles.builder()
                .withRoles(List.of(expectedRole))
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
    public void should_throw_user_not_found_when_get_role_for_authentication_for_an_unknown_user() {
        // Given
        when(authorizations.read()).thenReturn(
            UserRoles.builder().build()
        );

        // When
        assertThatThrownBy(() -> sut.userRoleById("unknown-user"))
            .isInstanceOf(UsernameNotFoundException.class);
    }
}
