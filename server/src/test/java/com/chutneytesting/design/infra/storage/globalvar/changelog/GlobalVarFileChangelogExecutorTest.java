package com.chutneytesting.design.infra.storage.globalvar.changelog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.infra.storage.globalvar.FileGlobalVarRepository;
import com.chutneytesting.tools.ThrowingConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GlobalVarFileChangelogExecutorTest {

    private static final String STORE_PATH = org.assertj.core.util.Files.temporaryFolderPath();
    private static GlobalVarFileChangelogExecutor globalVarFileChangelogExecutor;
    private static FileGlobalVarRepository globalVarRepository;

    @BeforeAll
    private static void init() {
        globalVarRepository = mock(FileGlobalVarRepository.class);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.walk(Paths.get(STORE_PATH + "/global_var"))
            .filter(Files::isRegularFile)
            .forEach(ThrowingConsumer.toUnchecked(Files::delete));
    }

    @Test
    public void should_migrate_hjson_file_to_yaml() {

        // Given
        String globalVarName = "migrate-me";
        String otherGlobalVarName = "migrate-me-too";
        String globalVarHjsonContent = "{\n" +
            "  queries:\n" +
            "  [\n" +
            "    {\n" +
            "      select:\n" +
            "      {\n" +
            "        scenarios: \"\"\n" +
            "        companies: \"\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        String globalVarYamlContent = "queries:\n" +
            "- select:\n" +
            "    scenarios: \"\"\n" +
            "    companies: \"\"\n";


        when(globalVarRepository.listOld()).thenReturn(Set.of(globalVarName, otherGlobalVarName));
        when(globalVarRepository.getOldFileContent(any())).thenReturn(globalVarHjsonContent);

        // When
        globalVarFileChangelogExecutor = new GlobalVarFileChangelogExecutor(globalVarRepository);

        // Then
        verify(globalVarRepository, times(1)).listOld();
        verify(globalVarRepository, times(1)).saveFile(globalVarName, globalVarYamlContent);
        verify(globalVarRepository, times(1)).saveFile(otherGlobalVarName, globalVarYamlContent);
        verify(globalVarRepository, times(1)).deleteOldFile(globalVarName);
        verify(globalVarRepository, times(1)).deleteOldFile(otherGlobalVarName);

    }

}
