package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.security.PropertyBasedTestingUtils;
import com.chutneytesting.security.api.AuthorizationsDto;
import com.chutneytesting.security.infra.JsonFileAuthorizations;
import com.chutneytesting.server.core.domain.security.Role;
import com.chutneytesting.server.core.domain.security.UserRoles;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class ChutneyAuthorizationsContentTest {

    private final JsonFileAuthorizations repository = mock(JsonFileAuthorizations.class);
    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

    @Property(tries=100)
    void should_return_authorizations_content_as_json(
        @ForAll("validUserRoles") UserRoles userRoles
    ) {
        // Given
        ChutneyAuthorizationsContent sut = new ChutneyAuthorizationsContent(repository, om);
        when(repository.read()).thenReturn(userRoles);
        // When
        List<ChutneyContent> contentList = sut.getContent().collect(toList());
        // Then
        assertThat(contentList).hasSize(1);
        ChutneyContent content = contentList.get(0);

        assertThat(content.name).isEqualTo("authorizations");
        assertThat(content.category).isEqualTo(CONF);
        assertThat(content.format).isEqualTo("json");

        AuthorizationsDto dto = assertContentIsReadableAsAuthorizationsDto(content);

        assert dto != null;
        assertRoles(dto, userRoles);
        assertAuthorizations(dto, userRoles);
    }

    private AuthorizationsDto assertContentIsReadableAsAuthorizationsDto(ChutneyContent content) {
        try {
            return om.readValue(content.content, AuthorizationsDto.class);
        } catch (Throwable t) {
            fail("Failed deserialize authorizations content ...", t);
        }
        return null;
    }

    private void assertRoles(AuthorizationsDto dto, UserRoles expected) {
        List<String> dtoRolesNames = dto.getRoles().stream().map(AuthorizationsDto.RoleDto::getName).collect(toList());
        List<String> expectedRolesNames = expected.roles().stream().map(r -> r.name).collect(toList());
        assertThat(dtoRolesNames).containsExactlyElementsOf(expectedRolesNames);

        dto.getRoles().forEach(role -> {
            List<String> expectedPermissions = expected.roleByName(role.getName()).authorizations.stream().map(Enum::name).collect(toList());
            assertThat(role.getRights()).containsExactlyElementsOf(expectedPermissions);
        });
    }

    private void assertAuthorizations(AuthorizationsDto dto, UserRoles expected) {
        dto.getAuthorizations().forEach(authorization -> {
            Set<String> expectedUsers = expected.usersByRole(Role.builder().withName(authorization.getName()).build()).stream().map(u -> u.id).collect(toSet());
            assertThat(authorization.getUsers()).containsExactlyInAnyOrderElementsOf(expectedUsers);
        });
    }

    @Provide
    @SuppressWarnings("unused")
    private Arbitrary<UserRoles> validUserRoles() {
        return PropertyBasedTestingUtils.validUserRoles();
    }
}
