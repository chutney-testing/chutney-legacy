package com.chutneytesting.environment;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.environment.infra.MigrateTargetSecurityExecutorTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EnvironmentConfigurationTest {

    @Test
    void should_give_access_to_api_service_and_repo_when_instantiated(@TempDir Path tempPath) {
        // When
        EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(tempPath.toString());
        // Then
        assertThat(environmentConfiguration.getEmbeddedEnvironmentApi()).isNotNull();
        assertThat(environmentConfiguration.getEnvironmentService()).isNotNull();
        assertThat(environmentConfiguration.getEnvironmentRepository()).isNotNull();
    }

    @Test
    void should_migrate_targets_security_when_instantiated(@TempDir Path envRootPath) throws Exception {
        // Given
        Path envPath = envRootPath.resolve("TO_MIGRATE.json");
        Files.copy(Path.of("target", "test-classes", "envs", "TO_MIGRATE.json"), envPath, StandardCopyOption.REPLACE_EXISTING);

        // When
        new EnvironmentConfiguration(envRootPath.toString());

        // Then
        MigrateTargetSecurityExecutorTest.assertThatEnvironmentTargetsHaveSecurityPropertiesCopiedInPropertiesNode(envPath);
    }
}
