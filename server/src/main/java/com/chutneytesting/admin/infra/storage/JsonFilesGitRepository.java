package com.chutneytesting.admin.infra.storage;

import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.design.infra.storage.scenario.git.GitRepository;
import com.chutneytesting.tools.file.FileUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class JsonFilesGitRepository {

    private static final Path ROOT_DIRECTORY_NAME = Paths.get("git");
    private final Path storeFolderPath;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    JsonFilesGitRepository(@Value("${chutney.configuration-folder:~/.chutney/conf}") String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME).toAbsolutePath();
        initFolder(this.storeFolderPath);
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

    private Path getPath(Long gitRepositoryId) {
        return storeFolderPath.resolve(gitRepositoryId + ".json");
    }
}
