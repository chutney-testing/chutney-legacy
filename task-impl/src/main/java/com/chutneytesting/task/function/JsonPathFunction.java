package com.chutneytesting.task.function;

import com.jayway.jsonpath.JsonPath;
import com.chutneytesting.task.spi.SpelFunction;
import com.chutneytesting.task.assertion.json.JsonUtils;

public class JsonPathFunction {

    @SpelFunction
    public static Object json(Object document, String jsonPath) {
        return JsonPath.parse(JsonUtils.jsonStringify(document)).read(jsonPath);
    }
}
