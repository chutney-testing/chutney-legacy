package com.chutneytesting.admin.infra.storage;

import static com.chutneytesting.admin.infra.storage.JsonHomePageRepository.HOME_PAGE_NAME;
import static com.chutneytesting.admin.infra.storage.JsonHomePageRepository.ROOT_DIRECTORY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.temporaryFolderPath;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;

public class JsonHomePageRepositoryTest {

    @Test
    public void should_backup_home_page_file_as_zip_file() throws IOException {
        // Given
        Path backup = Paths.get("./target/backup", "homepage");
        Files.createDirectories(backup.getParent());
        Files.deleteIfExists(backup);

        Path tempDirRoot = Paths.get(temporaryFolderPath()).resolve(ROOT_DIRECTORY_NAME);
        Files.createDirectories(tempDirRoot);
        Path homePagePath = tempDirRoot.resolve(HOME_PAGE_NAME);
        homePagePath.toFile().createNewFile();
        JsonHomePageRepository sut = new JsonHomePageRepository(tempDirRoot.getParent().toAbsolutePath().toString());

        try (OutputStream outputStream = Files.newOutputStream(Files.createFile(backup))) {
            // When
            sut.backup(outputStream);
        }

        // Then
        ZipFile zipFile = new ZipFile(backup.toString());
        List<String> entriesNames = zipFile.stream().map(ZipEntry::getName).collect(Collectors.toList());
        assertThat(entriesNames).containsExactly(HOME_PAGE_NAME);
    }

}
