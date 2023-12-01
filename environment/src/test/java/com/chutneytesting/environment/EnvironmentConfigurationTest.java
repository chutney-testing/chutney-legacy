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

package com.chutneytesting.environment;

import static com.chutneytesting.environment.infra.MigrateTargetSecurityExecutorTest.copyToMigrateEnvTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.infra.MigrateTargetSecurityExecutorTest;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EnvironmentConfigurationTest {

    @Test
    void should_give_access_to_api_when_instantiated(@TempDir Path tempPath) {
        // When
        EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(tempPath.toString());
        // Then
        assertThat(environmentConfiguration.getEmbeddedEnvironmentApi()).isNotNull();
    }

    @Test
    void should_migrate_targets_security_when_instantiated(@TempDir Path envRootPath) {
        // Given
        Path envPath = copyToMigrateEnvTo(envRootPath);

        // When
        new EnvironmentConfiguration(envRootPath.toString());

        // Then
        MigrateTargetSecurityExecutorTest.assertThatEnvironmentTargetsHaveSecurityPropertiesCopiedInPropertiesNode(envPath);
    }

    @Test
    void should_create_default_environment_at_started_when_environment_list_is_empty(@TempDir Path tempPath) {
        //Given
        EnvironmentDto expected = new EnvironmentDto("DEFAULT");

        // When
        EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(tempPath.toString());

        // Then
        assertThat(environmentConfiguration.getEmbeddedEnvironmentApi().listEnvironments().size()).isEqualTo(1);
        assertThat(environmentConfiguration.getEmbeddedEnvironmentApi().getEnvironment("DEFAULT")).isEqualTo(expected);
    }
}
