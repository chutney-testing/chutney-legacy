package com.chutneytesting.design.infra.storage.globalvar.changelog;

import com.chutneytesting.design.infra.storage.globalvar.FileGlobalVarRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.hjson.JsonValue;
import org.springframework.stereotype.Component;

@Component
public class GlobalVarFileChangelogExecutor {
    private final FileGlobalVarRepository globalVarRepository;

    public GlobalVarFileChangelogExecutor(FileGlobalVarRepository globalVarRepository) {
        this.globalVarRepository = globalVarRepository;
        this.migrateHjsonFiles();
    }

    public void migrateHjsonFiles() {
        globalVarRepository.listOld().stream()
            .forEach(globalVar -> {
                String hjson = globalVarRepository.getOldFileContent(globalVar);
                String yaml = this.hjsonToYaml(hjson);
                globalVarRepository.saveFile(globalVar, yaml);
                globalVarRepository.deleteOldFile(globalVar);
            });
    }

    private String hjsonToYaml(String hjson) {
        try {
            JsonNode jsonNodeTree = new ObjectMapper().readTree(JsonValue.readHjson(hjson).toString());
            return new YAMLMapper().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).writeValueAsString(jsonNodeTree);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not transform hjson to yaml");
        }
    }
}
