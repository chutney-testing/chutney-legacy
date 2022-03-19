package com.chutneytesting.design.infra.storage.globalvar;

import static com.chutneytesting.design.infra.storage.globalvar.FileGlobalVarRepository.ROOT_DIRECTORY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.tools.ThrowingConsumer;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class FileGlobalVarRepositoryTest {

    private static final String FILE_NAME = "global_var";
    private static final String STORE_PATH = org.assertj.core.util.Files.temporaryFolderPath();
    private FileGlobalVarRepository sut;

    @AfterEach
    public void tearDown() throws IOException {
        Files.walk(Paths.get(STORE_PATH + "/global_var"))
            .filter(Files::isRegularFile)
            .forEach(ThrowingConsumer.toUnchecked(Files::delete));
    }

    @Test
    public void shouldFlatKey() {
        // G
        sut = new FileGlobalVarRepository(STORE_PATH);

        String urlValue = "http://host:port/path";
        String mulitlineValuePattern = "" +
            "%sMy half empty glass,\n" +
            "%sI will fill your empty half.\n" +
            "%sNow you are half full.";
        String jsonPathValue = "//*[text()=\"${#spelRef}\"]//preceding::td[1]";

        sut.saveFile(FILE_NAME, "key1: " + urlValue + "\n" +
            "key2:\n" +
            "  subKey1: '''" + String.format(mulitlineValuePattern, "          ", "          ", "          ") + "\n" +
            "  subKey2: subValue2\n" +
            "key3:\n" +
            "- test1: value\n" +
            "  test2: value\n" +
            "- test: '''" + jsonPathValue + "'''" +
            "");

        // W
        Map<String, String> result = sut.getFlatMap();

        // T
        assertThat(result).containsOnly(
            entry("key1", urlValue),
            entry("key2.subKey1", String.format(mulitlineValuePattern, "", "", "")),
            entry("key2.subKey2", "subValue2"),
            entry("key3[0].test1", "value"),
            entry("key3[0].test2", "value"),
            entry("key3[1].test", jsonPathValue)
        );
    }

    @Test
    public void should_flat_keys_from_all_files() {
        // G
        sut = new FileGlobalVarRepository(STORE_PATH);
        sut.saveFile(FILE_NAME, "key1: value1\n" +
            "key2:\n" +
            "  subKey: subValue\n" +
            "key3:\n" +
            "- test1: value\n" +
            "  test2: value\n" +
            "- test: value");

        sut.saveFile("another_file", "keyA: valueA,\n" +
            "keyB:\n" +
            "  subKey: subValue\n" +
            "keyC:\n" +
            "- test1: value,\n" +
            "  test2: value\n" +
            "- test: value");

        // W
        Map<String, String> result = sut.getFlatMap();

        // T
        assertThat(result).containsOnly(
            entry("key1", "value1"),
            entry("key2.subKey", "subValue"),
            entry("key3[0].test1", "value"),
            entry("key3[0].test2", "value"),
            entry("key3[1].test", "value"),
            entry("keyA", "valueA"),
            entry("keyB.subKey", "subValue"),
            entry("keyC[0].test1", "value"),
            entry("keyC[0].test2", "value"),
            entry("keyC[1].test", "value")
        );
    }

    @Test
    public void aliasShouldOverrideKeyPath() {
        // G
        sut = new FileGlobalVarRepository(STORE_PATH);
        sut.saveFile(FILE_NAME, "menu:\n" +
            "  items:\n" +
            "  - id: Open\n" +
            "  - id: OpenNew\n" +
            "    label: Open New\n" +
            "  - alias: close\n" +
            "    id: Close\n" +
            "    label: Close Now");

        // W
        Map<String, String> result = sut.getFlatMap();

        // T
        assertThat(result).containsOnly(
            entry("menu.items[0].id", "Open"),
            entry("menu.items[1].label", "Open New"),
            entry("menu.items[1].id", "OpenNew"),
            entry("close.label", "Close Now"),
            entry("close.id", "Close")
        );
    }

    @Test
    public void should_backup_repository_directory_as_zip_file() throws IOException {
        // Given
        Path backup = Paths.get("./target/backup", "grv");
        Files.createDirectories(backup.getParent());
        Files.deleteIfExists(backup);

        sut = new FileGlobalVarRepository(STORE_PATH);
        sut.saveFile("a_file", "keyA: valueA");
        sut.saveFile("another_file", "keyB: valueB");

        try (OutputStream outputStream = Files.newOutputStream(Files.createFile(backup))) {
            // When
            sut.backup(outputStream);
        }

        // Then
        ZipFile zipFile = new ZipFile(backup.toString());
        List<String> entriesNames = zipFile.stream().map(ZipEntry::getName).collect(Collectors.toList());
        assertThat(entriesNames).containsExactlyInAnyOrder(
            ROOT_DIRECTORY_NAME.resolve("a_file.yml").toString(),
            ROOT_DIRECTORY_NAME.resolve("another_file.yml").toString()
        );
    }
}
