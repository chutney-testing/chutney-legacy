package com.chutneytesting.task.amqp.utils;

import static java.util.Spliterators.spliterator;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.StreamSupport;
import net.minidev.json.JSONArray;

public class JsonPathEvaluator {

    private static final Configuration CONFIG = Configuration.defaultConfiguration()
        .addOptions(Option.ALWAYS_RETURN_LIST)
        .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

    public static boolean evaluate(String jsonAsString, String jsonPath) {
        JSONArray ret = JsonPath.using(CONFIG).parse(jsonAsString).read(jsonPath);
        return StreamSupport.stream(spliterator(ret.iterator(), ret.size(), Spliterator.DISTINCT), false)
            .anyMatch(Objects::nonNull);
    }
}
