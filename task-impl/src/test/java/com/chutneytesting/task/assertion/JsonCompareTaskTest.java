package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class JsonCompareTaskTest {

    @ParameterizedTest
    @NullAndEmptySource
    void documents_must_not_be_blanks(String doc1) {
        // When
        int expectedNbErrors = doc1 == null ? 4 : 2;
        JsonCompareTask jsonAssertTask = new JsonCompareTask(mock(Logger.class), doc1, doc1, null, null);
        List<String> errors = jsonAssertTask.validateInputs();

        // Then
        assertThat(errors).hasSize(expectedNbErrors);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void should_compare_documents_roots_by_default(Map<String, String> paths) {
        // Given
        String doc1 = "{\"string\": \"string_value\", \"object\":{\"number\":3}, \"list\": [1, 2, 3]}";

        // When
        Logger mock = mock(Logger.class);
        JsonCompareTask jsonAssertTask = new JsonCompareTask(mock, doc1, doc1, paths, null);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
        verify(mock, times(2)).info(anyString());
    }

    @Test
    void should_execute_2_successful_assertions_on_comparing_actual_result_to_expected() {
        Map<String, String> pathInputs = new HashMap<>();
        pathInputs.put("$.something.value", "$.something_else.value");
        pathInputs.put("$.a_thing", "$.a_thing");

        // Given
        String doc1 = "{\"something\":{\"value\":3},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"my_type\"}}";
        String doc2 = "{\"something_else\":{\"value\":3},\"a_thing\":{\"type\":\"my_type\"}}";

        // When
        JsonCompareTask jsonAssertTask = new JsonCompareTask(mock(Logger.class), doc1, doc2, pathInputs, null);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    void should_failed_on_bad_assertions() {
        // Given
        Map<String, String> pathInputs = new HashMap<>();
        pathInputs.put("$.something.value", "$.something.value");//ko
        pathInputs.put("$.something_else.value", "$.something_else.value");//ok
        pathInputs.put("$.a_thing.type", "$.a_thing.type");//ko

        String doc1 = "{\"something\":{\"value\":3},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"type\"}}";
        String doc2 = "{\"something\":{\"value\":4},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"another_type\"}}";

        Logger mockLogger = mock(Logger.class);
        // When
        JsonCompareTask jsonAssertTask = new JsonCompareTask(mockLogger, doc1, doc2, pathInputs, null);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
        verify(mockLogger, times(2)).error(anyString());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("should do lenient comparison")
    class LenientComparison {

        @ParameterizedTest
        @DisplayName("classic equality is ok")
        @MethodSource("classic_equals")
        void lenient_should_be_ok_for_strict_json_equal(String doc) {
            // When
            Logger mock = mock(Logger.class);
            JsonCompareTask jsonAssertTask = new JsonCompareTask(mock, doc, doc, null, "lenient");
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(Success);
        }

        Stream<Arguments> classic_equals() {
            return Stream.of(
                Arguments.of("{}"),
                Arguments.of("[1, \"value\"]"),
                Arguments.of("{\"string\": \"value\"}"),
                Arguments.of("{\"number\": 666}"),
                Arguments.of("{\"array\": [1, 2, 3]}"),
                Arguments.of("{\"object\":{\"string\":\"value\"}}")
            );
        }

        @ParameterizedTest
        @MethodSource("extra_attributes")
        @DisplayName("extra attributes are ok for one document only")
        void lenient_should_be_ok_with_extra_attributes_for_one_document_only(String doc1, String doc2, TaskExecutionResult.Status expectedStatus) {
            // When
            Logger mock = mock(Logger.class);
            JsonCompareTask jsonAssertTask = new JsonCompareTask(mock, doc1, doc2, null, "LeNiEnT");
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(expectedStatus);
        }

        Stream<Arguments> extra_attributes() {
            return Stream.of(
                Arguments.of("{}", "{\"number\": 666}", Success),
                Arguments.of("{\"string\": \"val\"}", "{}", Success),
                Arguments.of("{}", "{\"array\": [666, \"val\", {\"att\": \"val\"}]}", Success),
                Arguments.of("{\"object\": {\"att\": \"val\"}}", "{}", Success),

                Arguments.of("{\"string\": \"val\"}", "{\"string\": \"val\", \"extra_number\": 666}", Success),
                Arguments.of("{\"string\": \"val\"}", "{\"string\": \"val\", \"extra_string\": \"value\"}", Success),
                Arguments.of("{\"string\": \"val\"}", "{\"string\": \"val\", \"extra_array\": [666, \"val\", {\"att\": \"val\"}]}", Success),
                Arguments.of("{\"string\": \"val\"}", "{\"string\": \"val\", \"extra_object\": {\"att\": \"val\"}}", Success),

                Arguments.of("{\"object\": {\"att\": \"val\"}}", "{\"object\": {\"att\": \"val\", \"extra_att\": \"extra_val\"}}", Success),

                Arguments.of("{\"string\": \"val\", \"extra_number_one\": 666}", "{\"string\": \"val\", \"extra_number_two\": 666}", Failure),
                Arguments.of("{\"string\": \"val\", \"object\": {\"att\": \"val\", \"extra_att_one\": \"val\"}}", "{\"string\": \"val\", \"object\": {\"att\": \"val\", \"extra_att_two\": \"val\"}}", Failure)
            );
        }

        @ParameterizedTest
        @MethodSource("array_order")
        @DisplayName("different array order is ok")
        void lenient_should_be_ok_with_different_array_order(String doc1, String doc2, TaskExecutionResult.Status expectedStatus) {
            // When
            Logger mock = mock(Logger.class);
            JsonCompareTask jsonAssertTask = new JsonCompareTask(mock, doc1, doc2, null, "LENIENT");
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(expectedStatus);
        }

        Stream<Arguments> array_order() {
            return Stream.of(
                Arguments.of("[1, 2, 3]", "[2, 3, 1]", Success),
                Arguments.of("[1, 2, 3, 1]", "[2, 3, 1]", Failure),
                Arguments.of("{\"array\": [1, null, 3]}", "{\"array\": [null, 3, 1]}", Success),
                Arguments.of("[null, 3]", "[null, 3, null]", Failure),

                Arguments.of("[{\"att\": \"val\"}, 3]}", "[3, {\"att\": \"val\"}]", Success),
                Arguments.of("{\"object\": {\"array\": [null, {\"att\": \"val\"}]}}", "{\"object\": {\"array\": [{\"att\": \"val\"}, null]}}", Success),
                Arguments.of("[{\"att\": \"val\"}]", "[{\"att\": \"val\"}, null]", Failure),

                Arguments.of("[1, [1, 2, 3], 3]", "[1, [3, 2, 1], 3]", Failure)
            );
        }
    }
}
