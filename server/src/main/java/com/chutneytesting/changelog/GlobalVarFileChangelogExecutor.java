package com.chutneytesting.changelog;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.LITERAL_BLOCK_STYLE;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.SPLIT_LINES;

import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.infra.storage.globalvar.FileGlobalVarRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.hjson.JsonValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GlobalVarFileChangelogExecutor {

    private static final String OLD_FILE_EXTENSION = ".hjson";

    private final GlobalvarRepository hjsonStoreRepository;
    private final GlobalvarRepository yamlStoreRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final YAMLMapper yamlMapper = new YAMLMapper()
        .enable(LITERAL_BLOCK_STYLE)
        .enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
        .disable(SPLIT_LINES);

    public GlobalVarFileChangelogExecutor(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath, GlobalvarRepository yamlStoreRepository) {
        this.hjsonStoreRepository = new FileGlobalVarRepository(storeFolderPath, OLD_FILE_EXTENSION);
        this.yamlStoreRepository = yamlStoreRepository;
    }

    public int migrateHjsonFiles() {
        Set<String> hjsonToMigrate = hjsonStoreRepository.list();
        hjsonToMigrate.forEach(globalVar -> {
            String hjson = hjsonStoreRepository.getFileContent(globalVar);
            String yaml = hjsonToYaml(hjson);
            yamlStoreRepository.saveFile(globalVar, yaml);
            hjsonStoreRepository.deleteFile(globalVar);
        });
        return hjsonToMigrate.size();
    }

    private String hjsonToYaml(String hjson) {
        try {
            String json = JsonValue.readHjson(hjson).toString();
            JsonNode jsonNodeTree = objectMapper.readTree(json);
            addLineBreakToSingleLineJsonText(jsonNodeTree);
            return yamlMapper.writeValueAsString(jsonNodeTree);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not transform hjson to yaml");
        }
    }

    private void addLineBreakToSingleLineJsonText(JsonNode jsonNodeTree) {
        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNodeTree.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> node = it.next();
            if (JsonNodeType.STRING.equals(node.getValue().getNodeType())) {
                try {
                    String textValue = node.getValue().textValue();
                    if (textValue.lines().count() == 1 && objectMapper.readTree(textValue) != null) {
                        ((ObjectNode) jsonNodeTree).set(node.getKey(), TextNode.valueOf(textValue + "\n"));
                    }
                } catch (JsonProcessingException e) { // Ignore non json string
                }
            } else {
                addLineBreakToSingleLineJsonText(node.getValue());
            }
        }
    }
}
