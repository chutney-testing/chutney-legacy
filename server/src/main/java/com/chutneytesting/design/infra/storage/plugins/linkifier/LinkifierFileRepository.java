package com.chutneytesting.design.infra.storage.plugins.linkifier;

import static com.chutneytesting.ServerConfigurationValues.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.design.domain.plugins.linkifier.Linkifier;
import com.chutneytesting.design.domain.plugins.linkifier.Linkifiers;
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
public class LinkifierFileRepository implements Linkifiers {

    private static final Path ROOT_DIRECTORY_NAME = Paths.get("plugins");
    private static final String LINKIFIER_FILE = "linkifiers.json";

    private final Path storeFolderPath;
    private final Path resolvedFilePath;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    LinkifierFileRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME);
        this.resolvedFilePath = this.storeFolderPath.resolve(LINKIFIER_FILE);
        initFolder(this.storeFolderPath);
    }

    @Override
    public List<Linkifier> getAll() {
        return getAll(resolvedFilePath);
    }

    private List<Linkifier> getAll(Path filePath) {
        return readFile(filePath).entrySet().stream()
            .map(e -> this.fromDto(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public Linkifier add(Linkifier linkifier) {
        Map<String, LinkifierDto> linkifiers = readDefaultFile();
        linkifiers.put(linkifier.id, toDto(linkifier));
        writeOnDisk(resolvedFilePath, linkifiers);
        return linkifier;
    }

    @Override
    public void remove(String id) {
        Map<String, LinkifierDto> linkifiers = readDefaultFile();
        linkifiers.remove(id);
        writeOnDisk(resolvedFilePath, linkifiers);
    }

    private Map<String, LinkifierDto> readDefaultFile() {
        return readFile(resolvedFilePath);
    }

    private Map<String, LinkifierDto> readFile(Path filePath) {
        Map<String, LinkifierDto> linkifiers = new HashMap<>();
        try {
            if (Files.exists(filePath)) {
                byte[] bytes = Files.readAllBytes(filePath);
                linkifiers.putAll(objectMapper.readValue(bytes, new TypeReference<HashMap<String, LinkifierDto>>() {}));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read configuration file: " + filePath, e);
        }

        return linkifiers;
    }

    private void writeOnDisk(Path filePath, Map<String, LinkifierDto> linkifiers) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(linkifiers);
            try {
                Files.write(filePath, bytes);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot write in configuration directory: " + storeFolderPath, e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot serialize " + linkifiers, e);
        }
    }


    public Linkifier fromDto(String id, LinkifierDto dto) {
        return new Linkifier(dto.pattern, dto.link, id);
    }

    public LinkifierDto toDto(Linkifier linkifier) {
        return new LinkifierDto(linkifier.pattern, linkifier.link);
    }
}
