package com.chutneytesting.task.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class YamlFunctionsTest {


    @ParameterizedTest
    @MethodSource("yamlNodeParams")
    public void should_get_yaml_node_by_path(String path, String key, String value) throws JsonProcessingException {
        String yaml = "key1:\n" +
            "  - key111: value\n" +
            "    key112: value\n" +
            "  - key12: value\n" +
            "key2: value";

        Object result = YamlFunctions.yamlPath(yaml, path);

        Assertions.assertThat(result).isInstanceOfSatisfying(Map.class, map -> Assertions.assertThat(map).containsEntry(key, value));
    }

    public static Stream<Arguments> yamlNodeParams() {
        return Stream.of(
            Arguments.of("$", "key2", "value"),
            Arguments.of("$.key1[0]", "key111", "value"),
            Arguments.of("$.key1[0]", "key112", "value")
        );
    }

}
