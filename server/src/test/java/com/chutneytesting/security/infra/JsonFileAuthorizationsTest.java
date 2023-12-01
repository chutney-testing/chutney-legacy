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

package com.chutneytesting.security.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.newTemporaryFolder;

import com.chutneytesting.security.PropertyBasedTestingUtils;
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
        assertThat(firstInit.roles()).isEmpty();
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
