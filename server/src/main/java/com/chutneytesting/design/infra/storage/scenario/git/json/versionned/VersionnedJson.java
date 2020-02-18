package com.chutneytesting.design.infra.storage.scenario.git.json.versionned;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class VersionnedJson {
    public final String version;
    public final JsonNode data;

    static VersionnedJson of(JsonNode node) {
        if(node.isObject() &&
            node.has("version") &&
            node.has("data") &&
            node.get("version").isTextual())
            return new VersionnedJson(node.get("version").asText(), node.get("data"));
        else return new VersionnedJson(null, node);
    }

    VersionnedJson(String version, JsonNode data) {
        this.version = version;
        this.data = data;
    }

    ObjectNode toNode() {
        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        result.set("version", new TextNode(version));
        result.set("data", data);
        return result;
    }
}
