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

package com.chutneytesting.server.core.domain.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(User.ANONYMOUS.roleName).isEqualTo("");
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
