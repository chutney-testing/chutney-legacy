package com.chutneytesting.design.infra.storage.globalvar;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.design.domain.globalvar.GlobalVarNotFoundException;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.tools.ZipUtils;
import com.chutneytesting.tools.file.FileUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileGlobalVarRepository implements GlobalvarRepository {

    private static final String FILE_EXTENSION = ".yml";
    private static final String OLD_FILE_EXTENSION = ".hjson";

    static final Path ROOT_DIRECTORY_NAME = Paths.get("global_var");


    private final Path storeFolderPath;

    private final ObjectMapper objectMapper = new YAMLMapper().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    FileGlobalVarRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME);
        initFolder(this.storeFolderPath);
    }

    @Override
    public Set<String> list() {
        return list(FILE_EXTENSION);
    }

    @Override
    public String getFileContent(String fileName) {
        return getFileContent(fileName, FILE_EXTENSION);
    }

    @Override
    public void saveFile(String fileName, String yamlContent) {
        Path filePath = this.storeFolderPath.resolve(fileName + FILE_EXTENSION);
        createFile(filePath);
        try {
            Files.write(filePath, yamlContent.getBytes());
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot save " + filePath.toUri(), e);
        }
    }

    private void createFile(Path filePath) {
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void deleteFile(String fileName) {
        deleteFile(fileName, FILE_EXTENSION);
    }

    @Override
    public Map<String, String> getFlatMap() {
        final Map<String, String> map = new HashMap<>();

        Map<Path, String> fileContents = get();
        fileContents.forEach((path, content) -> {
            try {
                addKeys("", objectMapper.readTree(content), map);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot deserialize global variable file: " + path, e);
            }
        });

        return map;
    }

    // TODO any - if needed, manage duplicate keys between files
    public Map<Path, String> get() {
        return FileUtils.doOnListFiles(storeFolderPath, (pathStream) ->
            pathStream
                .filter(Files::isRegularFile)
                .collect(Collectors.toMap(
                    p -> p,
                    FileUtils::readContent
                ))
        );
    }

    @Override
    public void backup(OutputStream outputStream) throws UncheckedIOException {
        try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
            Path globalVarDirectoryPath = this.storeFolderPath;
            ZipUtils.compressDirectoryToZipfile(globalVarDirectoryPath.getParent(), globalVarDirectoryPath.getFileName(), zipOutPut);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getOldFileContent(String fileName) {
        return getFileContent(fileName, OLD_FILE_EXTENSION);
    }

    public void deleteOldFile(String fileName) {
        deleteFile(fileName, OLD_FILE_EXTENSION);
    }

    private void deleteFile(String fileName, String extension) {
        Path filePath = this.storeFolderPath.resolve(fileName + extension);
        try {
            Files.delete(filePath);
        } catch (NoSuchFileException nsfe) {
            throw new GlobalVarNotFoundException(fileName);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot delete " + filePath.toUri(), e);
        }
    }

    private String getFileContent(String fileName, String extension) {
        Path filePath = this.storeFolderPath.resolve(fileName + extension);
        try {
            return new String(Files.readAllBytes(filePath));
        } catch (NoSuchFileException nsfe) {
            throw new GlobalVarNotFoundException(fileName);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read " + filePath.toUri(), e);
        }
    }

    public Set<String> listOld() {
        return list(OLD_FILE_EXTENSION);
    }

    public Path getStoreFolderPath() {
        return storeFolderPath;
    }

    private Set<String> list(String fileExtension) {
        return FileUtils.doOnListFiles(storeFolderPath, (pathStream) ->
            pathStream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(fileExtension))
                .map(FileUtils::getNameWithoutExtension)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private void addKeys(String currentPath, JsonNode jsonNode, Map<String, String> accumulator) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            String pathPrefix = getPrefixForObjectNode(currentPath, objectNode);
            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                if (!"alias".equals(entry.getKey())) {
                    addKeys(pathPrefix + entry.getKey(), entry.getValue(), accumulator);
                }
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                addKeys(currentPath + "[" + i + "]", arrayNode.get(i), accumulator);
            }
        } else if (jsonNode.isValueNode()) {
            ValueNode valueNode = (ValueNode) jsonNode;
            accumulator.put(currentPath, valueNode.asText());
        }
    }

    private String getPrefixForObjectNode(String currentPath, ObjectNode objectNode) {
        final Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            if ("alias".equals(entry.getKey())) {
                return entry.getValue().asText() + ".";
            }
        }
        return currentPath.isEmpty() ? "" : currentPath + ".";
    }
}
