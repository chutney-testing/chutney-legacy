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

package com.chutneytesting.environment.domain;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.NoEnvironmentFoundException;
import com.chutneytesting.environment.domain.exception.UnresolvedEnvironmentException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnvironmentServiceTest {

    EnvironmentService sut;
    EnvironmentRepository environmentRepository;

    @BeforeEach
    public void setUp() {
        environmentRepository = mock(EnvironmentRepository.class);
        sut = new EnvironmentService(environmentRepository);
    }

    @Test()
    public void create_environment_with_illegal_name_throws() {
        assertThatThrownBy(() -> sut.createEnvironment(Environment.builder().withName("illegal name").withDescription("some description").build()))
            .isInstanceOf(InvalidEnvironmentNameException.class);
    }

    @Test()
    public void update_environment_with_illegal_name_throws() {
        when(environmentRepository.findByName(any())).thenReturn(Environment.builder().withName("OLD_NAME").withDescription("some description").build());

        assertThatThrownBy(() -> sut.updateEnvironment("OLD_NAME", Environment.builder().withName("illegal name").withDescription("some description").build()))
            .isInstanceOf(InvalidEnvironmentNameException.class);
    }

    @Test
    void create_environment_should_throw_when_env_already_exist() {
        // Given
        when(environmentRepository.listNames())
            .thenReturn(List.of("EXISTING"));

        // Then
        assertThatThrownBy(() -> sut.createEnvironment(Environment.builder().withName("EXISTING").build()))
            .isInstanceOf(AlreadyExistingEnvironmentException.class);
    }

    @Test
    void create_environment_not_throw_when_env_already_exist_but_is_forced() {
        // Given
        Environment expected = Environment.builder().withName("EXISTING").build();
        when(environmentRepository.listNames())
            .thenReturn(List.of("EXISTING"));

        // When
        Environment actual = sut.createEnvironment(expected, true);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void should_return_the_unique_env_as_default() {
       // Given
        when(environmentRepository.listNames())
            .thenReturn(List.of("ENV"));

        // When
        String defaultEnv = sut.defaultEnvironmentName();

        // Then
        assertThat(defaultEnv).isEqualTo("ENV");

    }

    @Test
    void getting_default_env_should_throws_exception_no_env_found() {
        // Given
        when(environmentRepository.listNames())
            .thenReturn(emptyList());

        // When Then
        assertThatThrownBy(() -> sut.defaultEnvironmentName())
            .isInstanceOf(NoEnvironmentFoundException.class)
            .hasMessage("No Environment found");

    }

    @Test
    void getting_default_env_should_throws_exception_many_env_found() {
        // Given
        when(environmentRepository.listNames())
            .thenReturn(List.of("ENV", "OTHER"));

        // When Then
        assertThatThrownBy(() -> sut.defaultEnvironmentName())
            .isInstanceOf(UnresolvedEnvironmentException.class)
            .hasMessage("There is more than one environment. Could not resolve the default one");

    }
}
