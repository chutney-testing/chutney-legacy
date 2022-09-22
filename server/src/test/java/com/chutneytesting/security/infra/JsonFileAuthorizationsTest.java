package com.chutneytesting.security.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.newTemporaryFolder;

import com.chutneytesting.security.PropertyBasedTestingUtils;
import com.chutneytesting.server.core.domain.security.Role;
import com.chutneytesting.server.core.domain.security.UserRoles;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.BeforeTry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonFileAuthorizationsTest {

    private JsonFileAuthorizations sut;

    @BeforeEach
    @BeforeTry
    public void setUp() {
        sut = new JsonFileAuthorizations(newTemporaryFolder().getPath());
    }

    @Test
    public void should_init_authorizations_file_if_not_exists() {
        UserRoles firstInit = sut.read();
        assertThat(firstInit.roles()).containsExactly(Role.DEFAULT);
        assertThat(firstInit.users()).isEmpty();
    }

    @Property(tries = 100)
    public void should_save_then_read_authorizations_keeping_order(@ForAll("validUserRoles") UserRoles authorizations) {
        sut.save(authorizations);
        UserRoles read = sut.read();

        assertThat(read.roles()).containsExactlyElementsOf(authorizations.roles());
        assertThat(read.users()).containsExactlyElementsOf(authorizations.users());
    }

    @Provide
    @SuppressWarnings("unused")
    private Arbitrary<UserRoles> validUserRoles() {
        return PropertyBasedTestingUtils.validUserRoles();
    }
}
