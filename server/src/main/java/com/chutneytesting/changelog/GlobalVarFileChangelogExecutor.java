package com.chutneytesting.changelog;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.INDENT_ARRAYS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.SPLIT_LINES;

import com.chutneytesting.changelog.hjsontoyaml.YAMLFactory;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.infra.storage.globalvar.FileGlobalVarRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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
    private final YAMLMapper yamlMapper = new YAMLMapper(new YAMLFactory())
        .enable(MINIMIZE_QUOTES)
        .enable(INDENT_ARRAYS)
        .enable(INDENT_ARRAYS_WITH_INDICATOR)
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
            return yamlMapper.writeValueAsString(jsonNodeTree);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not transform hjson to yaml");
        }
    }
}
