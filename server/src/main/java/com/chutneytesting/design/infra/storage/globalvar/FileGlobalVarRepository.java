package com.chutneytesting.design.infra.storage.globalvar;

import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.tools.ZipUtils;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
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
import org.hjson.JsonValue;
import com.chutneytesting.tools.file.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileGlobalVarRepository implements GlobalvarRepository {

    private static final String FILE_EXTENSION = ".hjson";

    static final Path ROOT_DIRECTORY_NAME = Paths.get("global_var");

    private final Path storeFolderPath;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    FileGlobalVarRepository(@Value("${configuration-folder:conf}") String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME);
        initFolder(this.storeFolderPath);
    }

    @Override
    public Set<String> list() {
        return FileUtils.doOnListFiles(storeFolderPath, (pathStream) ->
            pathStream
                .filter(Files::isRegularFile)
                .map(FileUtils::getNameWithoutExtension)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    @Override
    public String getFileContent(String fileName) {
        Path filePath = this.storeFolderPath.resolve(fileName + FILE_EXTENSION);
        try {
            return new String(Files.readAllBytes(filePath));
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read " + filePath.toUri().toString(), e);
        }
    }

    @Override
    public void saveFile(String fileName, String hjsonContent) {
        Path filePath = this.storeFolderPath.resolve(fileName + FILE_EXTENSION);
        createFile(filePath);
        try {
            Files.write(filePath, hjsonContent.getBytes());
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot save " + filePath.toUri().toString(), e);
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
        Path filePath = this.storeFolderPath.resolve(fileName + FILE_EXTENSION);
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot delete " + filePath.toUri().toString(), e);
        }
    }

    @Override
    public Map<String, String> getFlatMap() {
        final Map<String, String> map = new HashMap<>();

        Map<Path, String> fileContents = get();
        fileContents.forEach((path, content) -> {
            try {
                addKeys("", objectMapper.readTree(JsonValue.readHjson(content).toString()), map);
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
