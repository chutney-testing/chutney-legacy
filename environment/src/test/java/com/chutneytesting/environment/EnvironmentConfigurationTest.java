package com.chutneytesting.environment;

import static com.chutneytesting.environment.infra.MigrateTargetSecurityExecutorTest.copyToMigrateEnvTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.environment.domain.Environment;
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
    void should_create_default_environment_when_instantiated(@TempDir Path tempPath) {
        // When
        EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(tempPath.toString());
        Environment expected = Environment.builder().withName("DEFAULT").build();
        // Then
        assertThat(environmentConfiguration.getEnvironmentRepository().getEnvironments().get(0)).isEqualTo(expected);
    }
}
