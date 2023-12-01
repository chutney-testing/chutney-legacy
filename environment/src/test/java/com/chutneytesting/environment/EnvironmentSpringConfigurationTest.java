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

import com.chutneytesting.environment.infra.MigrateTargetSecurityExecutorTest;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class EnvironmentSpringConfigurationTest {

    @Test
    void should_migrate_targets_security_when_used(@TempDir Path envRootPath) {
        // Given
        Path envPath = copyToMigrateEnvTo(envRootPath);

        // When
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getSystemProperties().put("chutney.environment.configuration-folder", envRootPath.toString());
        context.register(EnvironmentSpringConfiguration.class);
        context.refresh();

        // Then
        MigrateTargetSecurityExecutorTest.assertThatEnvironmentTargetsHaveSecurityPropertiesCopiedInPropertiesNode(envPath);
    }
}
