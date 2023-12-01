/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.function;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Test
    public void should_update_a_value_list_at_given_path() {

        String originalJson = "{\"dev\":{\"name\":\"Bruce\", \"needsCoffee\":false}}";

        String path = "$.dev.name";
        List<Integer> value = List.of(1, 2, 3, 4, 5);

        Object updatedJson = JsonFunctions.jsonSet(originalJson, path, value);

        assertThat(updatedJson).isEqualTo("{\"dev\":{\"name\":[1,2,3,4,5],\"needsCoffee\":false}}");
    }

    @Test
    public void should_update_a_value_null_object_at_given_path() {

        String originalJson = "{\"dev\":{\"name\":\"Bruce\", \"needsCoffee\":false}}";

        String path = "$.dev.name";

        Object updatedJson = JsonFunctions.jsonSet(originalJson, path, null);

        assertThat(updatedJson).isEqualTo("{\"dev\":{\"name\":null,\"needsCoffee\":false}}");
    }

    @Test
    public void should_update_a_value_empty_object_at_given_path() {

        String originalJson = "{\"dev\":{\"name\":\"Bruce\", \"needsCoffee\":false}}";

        String path = "$.dev.name";
        Object value = new Object();

        Object updatedJson = JsonFunctions.jsonSet(originalJson, path, value);

        assertThat(updatedJson).isEqualTo("{\"dev\":{\"name\":{},\"needsCoffee\":false}}");
    }

    @Test
    public void should_update_a_value_nested_object_at_given_path() {

        String originalJson = "{\"dev\":{\"name\":\"Bruce\", \"needsCoffee\":false}}";

        String path = "$.dev.name";
        Object value = Map.of("toto", Map.of("tata", "titi"));

        Object updatedJson = JsonFunctions.jsonSet(originalJson, path, value);

        assertThat(updatedJson).isEqualTo("{\"dev\":{\"name\":{\"toto\":{\"tata\":\"titi\"}},\"needsCoffee\":false}}");
    }

    @Test
    public void should_update_multiple_values_at_once_when_given_paths() {

        String originalJson = "{\"dev\":{\"name\":\"Bruce\", \"needsCoffee\":false}}";

        Map<String, Object> map = Map.of(
            "$.dev.name", "Batman",
            "$.dev.needsCoffee", true
        );

        String updatedJson = JsonFunctions.jsonSetMany(originalJson, map);

        assertThat(updatedJson).isEqualTo("{\"dev\":{\"name\":\"Batman\",\"needsCoffee\":true}}");
    }

    @Test
    public void should_update_multiple_values_of_any_type_at_once_when_given_paths() {

        String originalJson = "{\"dev\":{\"name\":\"Bruce\"}, \"needsCoffee\": false}";

        Map<String, Object> map = Map.of(
            "$.dev", Map.of("hero", Map.of("firstname", "Bruce", "name", "Wayne", "nickname", "Batman")),
            "$.needsCoffee", Optional.empty()
        );

        String updatedJson = JsonFunctions.jsonSetMany(originalJson, map);

        Map<String, Object> actualHero = (Map<String, Object>) JsonFunctions.jsonPath(updatedJson, "$.dev.hero");
        Object actualNeeds = JsonFunctions.jsonPath(updatedJson, "$.needsCoffee");

        assertThat(actualHero).containsExactlyInAnyOrderEntriesOf(Map.of(
            "name", "Wayne",
            "firstname", "Bruce",
            "nickname", "Batman"
        ));
        assertThat(actualNeeds).isEqualTo(emptyMap());
    }

    @Test
    public void should_merge_two_documents() {

        String documentA = "{\"firstName\":\"Bruce\"}";
        String documentB = "{\"lastName\":\"Wayne\"}";

        String updatedJson = JsonFunctions.jsonMerge(documentA, documentB);

        assertThat(updatedJson).isEqualTo("{\"firstName\":\"Bruce\",\"lastName\":\"Wayne\"}");
    }
}
