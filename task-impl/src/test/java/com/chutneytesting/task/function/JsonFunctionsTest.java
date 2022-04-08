package com.chutneytesting.task.function;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
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

        Object result = JsonFunctions.jsonPath(json, "$[0]");

        assertThat(result).isInstanceOfSatisfying(Map.class, map -> assertThat(map).containsEntry("key1", 42));
    }

    @Test
    public void value_is_extracted_when_single_in_array() {
        String json =
            "{" +
                "\"key1\": [\"value1\"]," +
                "\"key2\": \"gh\"" +
                "}";

        Object result = JsonFunctions.jsonPath(json, "$.key1");

        assertThat(result).isInstanceOfSatisfying(List.class, list -> assertThat(list).contains("value1"));
    }

    @Test
    public void should_update_a_value_at_given_path() {

        String originalJson = "{\"dev\":{\"name\":\"Bruce\", \"needsCoffee\":false}}";

        String path = "$.dev.name";
        String value = "Batman";

        Object updatedJson = JsonFunctions.jsonSet(originalJson, path, value);

        assertThat(updatedJson).isEqualTo("{\"dev\":{\"name\":\"Batman\",\"needsCoffee\":false}}");
    }
}
