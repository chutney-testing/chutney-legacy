package com.chutneytesting.agent.infra.storage;

import static com.chutneytesting.agent.infra.storage.JsonFileAgentNetworkDao.AGENTS_FILE_NAME;
import static com.chutneytesting.agent.infra.storage.JsonFileAgentNetworkDao.ROOT_DIRECTORY_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class JsonFileAgentNetworkDaoTest {

    private File file;
    private JsonFileAgentNetworkDao sut;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) throws Exception {
        sut = new JsonFileAgentNetworkDao(tempDir.toAbsolutePath().toString());
        Path filePath = tempDir.resolve(ROOT_DIRECTORY_NAME).resolve(AGENTS_FILE_NAME);
        file = Files.createFile(filePath).toFile();
        Files.writeString(filePath, "{}");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void read_should_return_the_network() {
        assertThat(sut.read()).isNotEmpty();
    }

    @Test
    public void read_without_existing_file_should_return_empty() throws IOException {
        Files.deleteIfExists(file.toPath());
        assertThat(sut.read()).isEmpty();
    }

    @Test
    public void should_save_agent_network() {
        sut.save(new AgentNetworkForJsonFile());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parallel_reading_is_possible() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> sut.read());
        executor.submit(() -> sut.read());
        executor.shutdown();
        assertThat(executor.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parallel_writing_is_possible() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> sut.save(new AgentNetworkForJsonFile()));
        executor.submit(() -> sut.save(new AgentNetworkForJsonFile()));
        executor.shutdown();
        assertThat(executor.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parallel_reading_writing_is_possible() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> sut.save(new AgentNetworkForJsonFile()));
        executor.submit(() -> {
            sleep(5);
            sut.read();
        });
        executor.shutdown();
        assertThat(executor.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parallel_writing_reading_is_possible() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> sut.read());
        executor.submit(() -> {
            sleep(5);
            sut.save(new AgentNetworkForJsonFile());
        });
        executor.shutdown();
        assertThat(executor.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    public void should_backup_json_file_as_zip_file() throws IOException {
        // Given
        Path backup = Paths.get("./target/backup", "endpoints");
        Files.createDirectories(backup.getParent());
        Files.deleteIfExists(backup);

        try (OutputStream outputStream = Files.newOutputStream(Files.createFile(backup))) {
            // When
            sut.backup(outputStream);
        }

        // Then
        ZipFile zipFile = new ZipFile(backup.toString());
        List<String> entriesNames = zipFile.stream().map(ZipEntry::getName).collect(Collectors.toList());
        assertThat(entriesNames).containsExactly(AGENTS_FILE_NAME);
    }

    private void sleep(long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
