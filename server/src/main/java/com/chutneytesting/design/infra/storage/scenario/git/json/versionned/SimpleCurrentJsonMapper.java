package com.chutneytesting.design.infra.storage.scenario.git.json.versionned;

import static com.chutneytesting.tools.Try.unsafe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class SimpleCurrentJsonMapper<T> implements CurrentJsonMapper<T>{
    private final String version;
    private final Class<T> type;
    private final ObjectMapper objectMapper;

    SimpleCurrentJsonMapper(Class<T> type, String version, ObjectMapper objectMapper) {
        this.version = version;
        this.type = type;
        this.objectMapper = objectMapper;
    }

    @Override public String version() {
        return version;
    }

    @Override public T readNode(JsonNode node) {
        return unsafe(() -> objectMapper.treeToValue(node, type) );
    }

    @Override public JsonNode toNode(T jsonObject) {
        return objectMapper.valueToTree(jsonObject);
    }
}
