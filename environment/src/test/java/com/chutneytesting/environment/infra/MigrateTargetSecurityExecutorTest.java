package com.chutneytesting.environment.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MigrateTargetSecurityExecutorTest {

    @Test
    void should_migrate_environment_targets_with_security_nodes_to_simple_properties(@TempDir Path envRootPath) throws Exception {
        // Given
        Path envPath = envRootPath.resolve("TO_MIGRATE.json");
        JsonFilesEnvironmentRepository environmentRepository = new JsonFilesEnvironmentRepository(envRootPath.toString());
        Files.copy(Path.of("target", "test-classes", "envs", "TO_MIGRATE.json"), envPath, StandardCopyOption.REPLACE_EXISTING);

        // When
        MigrateTargetSecurityExecutor sut = new MigrateTargetSecurityExecutor(environmentRepository);
        sut.execute();

        // Then
        assertThatEnvironmentTargetsHaveSecurityPropertiesCopiedInPropertiesNode(envPath);
    }

    public static void assertThatEnvironmentTargetsHaveSecurityPropertiesCopiedInPropertiesNode(Path envPath) throws IOException {
        JsonNode migratedEnvRootNode = new ObjectMapper().readTree(Files.readAllBytes(envPath));
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
