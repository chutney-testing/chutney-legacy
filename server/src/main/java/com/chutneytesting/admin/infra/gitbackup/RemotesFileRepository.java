package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.tools.file.FileUtils.initFolder;
import static java.util.Optional.ofNullable;

import com.chutneytesting.admin.domain.BackupNotFoundException;
import com.chutneytesting.admin.domain.gitbackup.RemoteRepository;
import com.chutneytesting.admin.domain.gitbackup.Remotes;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RemotesFileRepository implements Remotes {

    private static final Path ROOT_DIRECTORY_NAME = Paths.get("plugins");
    private static final String REMOTES_FILE = "git-remotes.json";

    private final Path storeFolderPath;
    private final Path resolvedFilePath;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public RemotesFileRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME);
        this.resolvedFilePath = this.storeFolderPath.resolve(REMOTES_FILE);
        initFolder(this.storeFolderPath);
    }

    @Override
    public List<RemoteRepository> getAll() {
        return readFromDisk().values().stream()
            .map(this::fromDto)
            .collect(Collectors.toList());
    }

    @Override
    public RemoteRepository add(RemoteRepository remote) {
        Map<String, GitRemoteDto> remotes = readFromDisk();
        remotes.put(remote.name, toDto(remote));
        writeOnDisk(resolvedFilePath, remotes);
        return remote;
    }

    @Override
    public void remove(String name) {
        Map<String, GitRemoteDto> remotes = readFromDisk();
        ofNullable(remotes.remove(name))
            .ifPresent(r -> writeOnDisk(resolvedFilePath, remotes));
    }

    @Override
    public RemoteRepository get(String name) {
        return ofNullable(readFromDisk().get(name))
            .map(this::fromDto)
            .orElseThrow(() -> new BackupNotFoundException(name));
    }

    private Map<String, GitRemoteDto> readFromDisk() {
        Map<String, GitRemoteDto> remotes = new HashMap<>();
        try {
            if (Files.exists(resolvedFilePath)) {
                byte[] bytes = Files.readAllBytes(resolvedFilePath);
                remotes.putAll(objectMapper.readValue(bytes, new TypeReference<HashMap<String, GitRemoteDto>>() {
                }));
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot read configuration file: " + resolvedFilePath, e);
        }

        return remotes;
    }

    private void writeOnDisk(Path filePath, Map<String, GitRemoteDto> remotes) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(remotes);
            try {
                Files.write(filePath, bytes);
            } catch (IOException e) {
                throw new RuntimeException("Cannot write in configuration directory: " + storeFolderPath, e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot serialize " + remotes, e);
        }
    }

    private RemoteRepository fromDto(GitRemoteDto dto) {
        return new RemoteRepository(dto.name, dto.url, dto.branch, dto.privateKeyPath, dto.privateKeyPassphrase);
    }

    private GitRemoteDto toDto(RemoteRepository remote) {
        return new GitRemoteDto(remote.name, remote.url, remote.branch, remote.privateKeyPath, remote.privateKeyPassphrase);
    }
}
