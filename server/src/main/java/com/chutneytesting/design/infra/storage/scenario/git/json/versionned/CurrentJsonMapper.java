package com.chutneytesting.design.infra.storage.scenario.git.json.versionned;

import com.fasterxml.jackson.databind.JsonNode;

public interface CurrentJsonMapper<T> extends VersionnedJsonReader<T> {
    JsonNode toNode(T jsonObject);
}
