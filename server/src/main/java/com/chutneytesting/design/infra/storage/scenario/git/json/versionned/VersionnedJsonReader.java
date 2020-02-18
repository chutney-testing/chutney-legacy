package com.chutneytesting.design.infra.storage.scenario.git.json.versionned;

import com.fasterxml.jackson.databind.JsonNode;

public interface VersionnedJsonReader<T> {
    /**
     * version of the mapper. Use null to handle json without version
     */
    String version();

    T readNode(JsonNode node);
}
