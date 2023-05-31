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
