package com.chutneytesting.environment;

import static com.chutneytesting.environment.infra.MigrateTargetSecurityExecutorTest.copyToMigrateEnvTo;

import com.chutneytesting.environment.infra.MigrateTargetSecurityExecutorTest;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class EnvironmentSpringConfigurationTest {

    @AfterEach
    void cleanUp() {
        System.clearProperty("chutney.environment.configuration-folder");
    }

    @Test
    void should_migrate_targets_security_when_used(@TempDir Path envRootPath) {
        // Given
        System.setProperty("chutney.environment.configuration-folder", envRootPath.toString());
        Path envPath = copyToMigrateEnvTo(envRootPath);

        // When
        new AnnotationConfigApplicationContext(EnvironmentSpringConfiguration.class);

        // Then
        MigrateTargetSecurityExecutorTest.assertThatEnvironmentTargetsHaveSecurityPropertiesCopiedInPropertiesNode(envPath);
    }
}
