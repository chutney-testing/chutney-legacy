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

package com.chutneytesting.environment.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MigrateTargetSecurityExecutorTest {

    @Test
    void should_migrate_environment_targets_with_security_nodes_to_simple_properties(@TempDir Path envRootPath) {
        // Given
        Path envPath = copyToMigrateEnvTo(envRootPath);
        JsonFilesEnvironmentRepository environmentRepository = new JsonFilesEnvironmentRepository(envRootPath.toString());

        // When
        MigrateTargetSecurityExecutor sut = new MigrateTargetSecurityExecutor(environmentRepository);
        sut.execute();

        // Then
        assertThatEnvironmentTargetsHaveSecurityPropertiesCopiedInPropertiesNode(envPath);
    }

    public static Path copyToMigrateEnvTo(Path tmpPath) {
        try {
            String toMigrateEnv = "TO_MIGRATE.json";
            Path envPath = tmpPath.resolve(toMigrateEnv);
            Files.copy(Path.of("target", "test-classes", "envs", toMigrateEnv), envPath, StandardCopyOption.REPLACE_EXISTING);
            return envPath;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void assertThatEnvironmentTargetsHaveSecurityPropertiesCopiedInPropertiesNode(Path envPath) {
        JsonNode migratedEnvRootNode;
        try {
            migratedEnvRootNode = new ObjectMapper().readTree(Files.readAllBytes(envPath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        assert migratedEnvRootNode != null;
        migratedEnvRootNode.get("targets").forEach(target -> {
            assertThat(target.get("url").textValue()).isEqualTo("url");
            assertThat(target.get("security")).isNull();

            JsonNode targetProperties = target.get("properties");
            if ("WITH_SECURITY_WITH_PROPERTIES".equals(target.get("name").textValue())) {
                assertThat(targetProperties.get("key").textValue()).isEqualTo("value");
                assertThat(targetProperties.get("username").textValue()).isEqualTo("a.user");
                assertThat(targetProperties.get("password").textValue()).isEqualTo("user.password");
                assertThat(targetProperties.get("trustStore").textValue()).isEqualTo("path/to/truststore");
                assertThat(targetProperties.get("trustStorePassword").textValue()).isEqualTo("truststore.password");
                assertThat(targetProperties.get("keyStore").textValue()).isEqualTo("path/to/keystore");
                assertThat(targetProperties.get("keyStorePassword").textValue()).isEqualTo("keystore.password");
                assertThat(targetProperties.get("privateKey").textValue()).isEqualTo("path/to/privatekey");
                assertThat(targetProperties.get("keyPassword").textValue()).isEqualTo("privatekey.password");
            } else if ("WITH_SECURITY_WITHOUT_USER_PASS_WITHOUT_PROPERTIES".equals(target.get("name").textValue())) {
                assertThat(targetProperties.get("username").textValue()).isEqualTo("a.user");
                assertThat(targetProperties.get("password")).isNull();
                assertThat(targetProperties.get("trustStore").textValue()).isEqualTo("path/to/truststore");
                assertThat(targetProperties.get("trustStorePassword").textValue()).isEqualTo("truststore.password");
                assertThat(targetProperties.get("keyStore").textValue()).isEqualTo("path/to/keystore");
                assertThat(targetProperties.get("keyStorePassword").textValue()).isEqualTo("keystore.password");
                assertThat(targetProperties.get("privateKey")).isNull();
                assertThat(targetProperties.get("keyPassword")).isNull();
            } else if ("WITHOUT_SECURITY".equals(target.get("name").textValue())) {
                assertThat(targetProperties.get("key").textValue()).isEqualTo("value");
            } else {
                Assertions.fail("Unknown target in migrated file !!");
            }
        });
    }
}
