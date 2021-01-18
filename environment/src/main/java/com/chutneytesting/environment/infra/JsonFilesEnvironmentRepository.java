package com.chutneytesting.environment.infra;

import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.environment.domain.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.InvalidEnvironmentNameException;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class JsonFilesEnvironmentRepository implements EnvironmentRepository {

    private static final String NAME_VALIDATION_REGEX = "[a-zA-Z0-9_\\-]{3,20}";
    private static final Pattern NAME_VALIDATION_PATTERN = Pattern.compile(NAME_VALIDATION_REGEX);
    private static final String JSON_FILE_EXT = ".json";

    private final Path storeFolderPath;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public JsonFilesEnvironmentRepository(@Value("${configuration-folder:conf}") String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).toAbsolutePath();
        initFolder(this.storeFolderPath);
    }

    @Override
    public synchronized void save(Environment environment) throws UnsupportedOperationException, InvalidEnvironmentNameException {
        if (!NAME_VALIDATION_PATTERN.matcher(environment.name).matches()) {
            throw new InvalidEnvironmentNameException("Environment name must be of 3 to 20 letters, digits, underscore or hyphen");
        }
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
                // TODO any - Use sub-repositories instead
                .filter(this::isNotAgentConfigurationFile)
                .map(FileUtils::getNameWithoutExtension)
                .collect(Collectors.toList())
        );
    }

    private boolean isNotAgentConfigurationFile(Path path) {
        return !"endpoints.json".equals(path.getFileName().toString());
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
