package com.chutneytesting.task.function;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.task.assertion.utils.JsonUtils;
import com.chutneytesting.task.spi.SpelFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

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
}
