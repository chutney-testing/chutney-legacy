package com.chutneytesting.admin.infra.storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.chutneytesting.design.infra.storage.scenario.git.GitRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import com.chutneytesting.tools.file.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * TODO create abstract file persistence repository. Same as JsonFilesEnvironmentRepository.
 */
@Repository
public class JsonFilesGitRepository {

    private final Path storeFolderPath;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    JsonFilesGitRepository(@Value("${git-configuration-folder:git-conf}") String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).toAbsolutePath();
        initFolder();
    }

    public synchronized void save(GitRepository gitRepository) throws UnsupportedOperationException {
        // TODO check if id exists
        Path gitRepositoryPath = getPath(gitRepository.id);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(gitRepository);
            try {
                Files.write(gitRepositoryPath, bytes);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot save in configuration directory: " + storeFolderPath, e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot serialize " + gitRepository, e);
        }
    }

    public Set<GitRepository> listGitRepository() {
        return FileUtils.doOnListFiles(storeFolderPath, pathStream ->
            pathStream
                .filter(Files::isRegularFile)
                .filter(this::isJsonFile)
                .map(path -> {
                    try {
                        byte[] bytes = Files.readAllBytes(path);
                        return objectMapper.readValue(bytes, GitRepository.class);
                    } catch (IOException e) {
                        throw new UnsupportedOperationException("Cannot deserialize git-configuration file: " + path, e);
                    }
                }).collect(Collectors.toSet())
        );
    }

    private boolean isJsonFile(Path path) {
        return path.getFileName().toString().endsWith(".json");
    }

    public void delete(Long gitRepositoryId) {
        // TODO check if id exists
        Path gitRepositoryPath = getPath(gitRepositoryId);
        if (!Files.exists(gitRepositoryPath)) {
            throw new IllegalArgumentException("Git-configuration file not found: " + gitRepositoryPath);
        }
        try {
            Files.delete(gitRepositoryPath);
        } catch (IOException e) {
            throw new RuntimeException("Cannot delete git-configuration file: " + gitRepositoryPath, e);
        }
    }

    private void initFolder() throws UncheckedIOException {
        try {
            Files.createDirectories(storeFolderPath);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create configuration directory: " + storeFolderPath, e);
        }

        Path testPath = storeFolderPath.resolve("test");
        if (!Files.exists(testPath)) {
            try {
                Files.createFile(storeFolderPath.resolve("test"));
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to initFolder in configuration directory: " + storeFolderPath, e);
            }
        }

        try {
            Files.delete(testPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to initFolder in configuration directory: " + storeFolderPath, e);
        }
    }

    private Path getPath(Long gitRepositoryId) {
        return storeFolderPath.resolve(gitRepositoryId + ".json");
    }
}
