package com.chutneytesting.action.function;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.action.common.JsonUtils;
import com.chutneytesting.action.spi.SpelFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonFunctions {

    private static final ObjectMapper om = new ObjectMapper();

    @Deprecated
    @SpelFunction
    public static Object json(Object document, String jsonPath) {
        return jsonPath(document, jsonPath);
    }

    @SpelFunction
    public static Object jsonPath(Object document, String jsonPath) {
        return JsonPath.parse(JsonUtils.jsonStringify(document)).read(jsonPath);
    }

    @SpelFunction
    public static String jsonSerialize(Object obj) throws JsonProcessingException {
        return om.writeValueAsString(requireNonNull(obj));
    }

    @SpelFunction
    public static String jsonSet(Object document, String path, String value) {
        return JsonPath.parse(JsonUtils.jsonStringify(document))
            .set(path, value)
            .jsonString();
    }

    @SpelFunction
    public static String jsonSetMany(Object document, Map<String, Object> map) {
        DocumentContext jsonDocument = JsonPath.parse(JsonUtils.jsonStringify(document));
        map.forEach(jsonDocument::set);
        return jsonDocument.jsonString();
    }

    @SpelFunction
    public static String jsonMerge(Object documentA, Object documentB) {
        LinkedHashMap jsonDocA = JsonPath.parse(JsonUtils.jsonStringify(documentA)).json();
        LinkedHashMap jsonDocB = JsonPath.parse(JsonUtils.jsonStringify(documentB)).json();

        jsonDocA.putAll(jsonDocB);

        return JsonPath.parse(JsonUtils.jsonStringify(jsonDocA)).jsonString();
    }

}
