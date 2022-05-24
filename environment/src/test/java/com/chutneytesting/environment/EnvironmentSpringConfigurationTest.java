package com.chutneytesting.environment;

import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.environment.infra.MigrateTargetSecurityExecutorTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    void should_migrate_targets_security_when_used(@TempDir Path envRootPath) throws Exception {
        // Given
        System.setProperty("chutney.environment.configuration-folder", envRootPath.toString());
        initFolder(envRootPath);
        Path envPath = envRootPath.resolve("TO_MIGRATE.json");
        EnvironmentSpringConfigurationTest.class.getResource("envs/TO_MIGRATE.json");
        Files.copy(Path.of("target", "test-classes", "envs", "TO_MIGRATE.json"), envPath, StandardCopyOption.REPLACE_EXISTING);

        // When
        new AnnotationConfigApplicationContext(EnvironmentSpringConfiguration.class);

        // Then
        MigrateTargetSecurityExecutorTest.assertThatEnvironmentTargetsHaveSecurityPropertiesCopiedInPropertiesNode(envPath);
    }
}
