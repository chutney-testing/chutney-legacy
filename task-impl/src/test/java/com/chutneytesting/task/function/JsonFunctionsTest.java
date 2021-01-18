package com.chutneytesting.task.function;

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class JsonFunctionsTest {

    @Test
    public void result_is_escaped_for_further_serialization() throws JSONException {
        String json =
            "[{" +
                "\"key1\": 42," +
                "\"key2\": \"gh\"" +
                "}, {}]";

        Object result = JsonFunctions.json(json, "$[0]");

        Assertions.assertThat(result).isInstanceOfSatisfying(Map.class, map -> Assertions.assertThat(map).containsEntry("key1", 42));
    }

    @Test
    public void value_is_extracted_when_single_in_array() {
        String json =
            "{" +
                "\"key1\": [\"value1\"]," +
                "\"key2\": \"gh\"" +
                "}";

        Object result = JsonFunctions.json(json, "$.key1");

        Assertions.assertThat(result).isInstanceOfSatisfying(List.class, list -> Assertions.assertThat(list).contains("value1"));
    }
}
