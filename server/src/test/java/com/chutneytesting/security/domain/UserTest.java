package com.chutneytesting.security.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.security.PropertyBasedTestingUtils;
import com.chutneytesting.server.core.security.Role;
import com.chutneytesting.server.core.security.User;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    public void should_build_anonymous_user_with_default_role() {
        User defaultBuild = User.builder().build();
        assertThat(defaultBuild).isEqualTo(User.ANONYMOUS);

        User nullBuild = User.builder().withId(null).withRole(null).build();
        assertThat(nullBuild).isEqualTo(User.ANONYMOUS);

        assertThat(User.ANONYMOUS.roleName).isEqualTo(Role.DEFAULT.name);
    }

    @Test
    public void anonymous_user_id_cannot_be_used() {
        assertThatThrownBy(() ->
            User.builder().withId(User.ANONYMOUS.id).build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_test_anonymous_user_id() {
        assertThat(User.isAnonymous(User.ANONYMOUS.id)).isTrue();
        assertThat(User.isAnonymous("NOT_ANONYMOUS")).isFalse();
    }

    @Property
    public void should_build_user(@ForAll("validUserId") String userId) {
        assertThat(
            User.builder().withId(userId).build()
        ).isNotNull();
    }

    @Property
    public void should_validate_user_id(@ForAll("invalidUserId") String userId) {
        assertThatThrownBy(() ->
            User.builder().withId(userId).build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Provide
    @SuppressWarnings("unused")
    private Arbitrary<String> validUserId() {
        return PropertyBasedTestingUtils.validUserId();
    }

    @Provide
    @SuppressWarnings("unused")
    private Arbitrary<String> invalidUserId() {
        return PropertyBasedTestingUtils.invalidUserId();
    }
}
