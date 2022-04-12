package com.chutneytesting.changelog;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.infra.storage.globalvar.FileGlobalVarRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GlobalVarFileChangelogExecutorTest {

    static final String RESOURCE_FOLDER = "/unittest/com/chutneytesting/changelog";

    @TempDir
    static Path temporaryFolder;

    GlobalVarFileChangelogExecutor globalVarFileChangelogExecutor;
    String hjsonExample;

    @BeforeEach
    private void setUp() throws Exception {
        hjsonExample = new String(GlobalVarFileChangelogExecutorTest.class.getResourceAsStream(RESOURCE_FOLDER + "/example.hjson").readAllBytes());
        FileGlobalVarRepository yamlRepository = new FileGlobalVarRepository(temporaryFolder.toString());
        globalVarFileChangelogExecutor = new GlobalVarFileChangelogExecutor(temporaryFolder.toString(), yamlRepository);
    }

    @Test
    public void should_migrate_hjson_file_to_yaml() throws Exception {
        Path source = Paths.get(GlobalVarFileChangelogExecutorTest.class.getResource(RESOURCE_FOLDER + "/example.hjson").toURI());
        Path target = temporaryFolder.resolve("global_var").resolve("example.hjson");

        // Given
        Files.copy(source, target);

        // When
        globalVarFileChangelogExecutor.migrateHjsonFiles();

        // Then
        String expectedYaml = new String(getClass().getResourceAsStream(RESOURCE_FOLDER + "/expectedExample.yml").readAllBytes());
        String actualYaml = Files.readString(temporaryFolder.resolve("global_var").resolve("example.yml"));

        assertThat(actualYaml).isEqualTo(expectedYaml);
    }
}
