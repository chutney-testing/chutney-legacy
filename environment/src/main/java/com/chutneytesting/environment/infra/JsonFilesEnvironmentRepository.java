package com.chutneytesting.environment.infra;

import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.exception.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.tools.file.FileUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonFilesEnvironmentRepository implements EnvironmentRepository {

    static final Path ROOT_DIRECTORY_NAME = Paths.get("env");
    private static final String JSON_FILE_EXT = ".json";

    private final Path storeFolderPath;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public JsonFilesEnvironmentRepository(String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME).toAbsolutePath();
        initFolder(this.storeFolderPath);
    }

    @Override
    public synchronized void save(Environment environment) throws UnsupportedOperationException, InvalidEnvironmentNameException {
        doSave(environment);
    }

    @Override
    public Environment findByName(String name) throws EnvironmentNotFoundException {
        Path environmentPath = getEnvironmentPath(name);
        if (!Files.exists(environmentPath)) {
            throw new EnvironmentNotFoundException("Configuration file not found: " + environmentPath);
        }
        try {
            byte[] bytes = Files.readAllBytes(environmentPath);
            try {
                return objectMapper.readValue(bytes, JsonEnvironment.class).toEnvironment();
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot deserialize configuration file: " + environmentPath, e);
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read configuration file: " + environmentPath, e);
        }
    }

    @Override
    public List<String> listNames() throws UnsupportedOperationException {
        return FileUtils.doOnListFiles(storeFolderPath, (pathStream) ->
            pathStream
                .filter(Files::isRegularFile)
                .filter(this::isJsonFile)
                .map(FileUtils::getNameWithoutExtension)
                .collect(Collectors.toList())
        );
    }

    private boolean isJsonFile(Path path) {
        return path.getFileName().toString().endsWith(JSON_FILE_EXT);
    }

    @Override
    public void delete(String name) {
        Path environmentPath = getEnvironmentPath(name);
        if (!Files.exists(environmentPath)) {
            throw new EnvironmentNotFoundException("Configuration file not found: " + environmentPath);
        }
        try {
            Path backupPath = Paths.get(environmentPath.toString() + UUID.randomUUID().getMostSignificantBits() + ".backup");
            Files.move(environmentPath, backupPath);
        } catch (IOException e) {
            throw new CannotDeleteEnvironmentException("Cannot delete configuration file: " + environmentPath, e);
        }
    }

    private void doSave(Environment environment) {
        Path environmentPath = getEnvironmentPath(environment.name);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(JsonEnvironment.from(environment));
            try {
                Files.write(environmentPath, bytes);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot write in configuration directory: " + storeFolderPath, e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot serialize " + environment, e);
        }
    }

    private Path getEnvironmentPath(String name) {
        return storeFolderPath.resolve(name + JSON_FILE_EXT);
    }
}
