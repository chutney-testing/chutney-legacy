package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.assertion.JsonCompareTask.COMPARE_MODE;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JsonAssertTaskTest {

    @Test
    void should_take_zoned_date_when_asserting_dates() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.onedate", "$isBeforeDate:2020-08-14T15:07:46.621Z");
        expected.put("$.something.seconddate", "$isAfterDate:2020-08-14T16:56:56+02:00");
        expected.put("$.something.thirddate", "$isEqualDate:2020-08-14T17:07:46.621+02:00");

        // Given
        String fakeActualResult = "{" +
            "\"something\": {" +
            "\"onedate\":\"2020-08-14T16:56:56+02:00\"," +
            "\"seconddate\":\"2020-08-14T15:07:46.621Z\"," +
            "\"thirddate\":\"2020-08-14T15:07:46.621Z\"" +
            "}" +
            "}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), fakeActualResult, expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    void should_execute_4_successful_assertions_on_comparing_actual_result_to_expected() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", 3);
        expected.put("$.something_else.value", 5);
        expected.put("$.a_thing.type", "my_type");
        expected.put("$.a_thing.not.existing", null);

        // Given
        String fakeActualResult = "{\"something\":{\"value\":3},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"my_type\"}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    void should_execute_a_failing_assertion_on_comparing_actual_result_to_expected() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", 42);

        // Given
        String fakeActualResult = "{\"something\":{\"value\":3}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @Test
    void should_execute_a_failing_assertion_on_invalid_JSON_content_in_actual() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", 1);

        // Given
        String fakeInvalidJson = "{\"EXCEPTION 42 - BSOD\"}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class), fakeInvalidJson, expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @Test
    void should_execute_a_failing_assertion_on_wrong_XPath_value_in_expected() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.wrong.json.x.xpath", 1);

        // Given
        String fakeActualResult = "{\"xpath\":{\"to\":\"value\"}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @Test
    void should_not_convert_int_as_long() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", 400.0);

        // Given
        String fakeActualResult = "{\"something\":{\"value\":400.0}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    void should_assert_enum_as_string() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", COMPARE_MODE.STRICT);

        // Given
        String fakeActualResult = "{\"something\":{\"value\":\"STRICT\"}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    void should_execute_a_successful_assertions_on_comparing_expected_value_as_string_and_actual_value_as_number() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", "my_value");

        // Given
        String fakeActualResult = "{\"something\":{\"value\": my_value}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class), fakeActualResult, expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Assert json using placeholders")
    class AssertWithPlaceholders {

        @Test
        @DisplayName("isNull")
        public void should_assert_with_isNull_placeholder() {
            Map<String, Object> expected = new HashMap<>();
            expected.put("$.something.notexist", "$isNull");
            expected.put("$.something[?(@.notexist=='noop')]", "$isNull");
            expected.put("$.valuenull", "$isNull");

            // Given
            String fakeActualResult = "{ \"valuenull\":null }";

            // When
            JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), fakeActualResult, expected);
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(Success);
        }

        @Test
        @DisplayName("isNotNull")
        public void should_assert_with_isNotNull_placeholder() {
            Map<String, Object> expected = new HashMap<>();
            expected.put("$.something", "$isNotNull");
            expected.put("$.something.value", "$isNotNull");
            expected.put("$.something.emptyObject", "$isNotNull");
            expected.put("$.something.emptyArray", "$isNotNull");
            expected.put("$.something.emptyString", "$isNotNull");

            // Given
            String fakeActualResult = "{" +
                "\"something\": {" +
                "\"value\": 3," +
                "\"valuenull\":null," +
                "\"emptyObject\": {}," +
                "\"emptyArray\": []," +
                "\"emptyString\": \"\"" +
                "}" +
                "}";

            // When
            JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), fakeActualResult, expected);
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(Success);
        }

        @Test
        @DisplayName("contains")
        public void should_assert_with_contains_placeholder() {
            Map<String, Object> expected = new HashMap<>();
            expected.put("$.something.alphabet", "$contains:def");
            expected.put("$.something.alphabet", "$contains:abcdefg");
            expected.put("$.something.value", "$contains:3");
            expected.put("$", "$contains:636, alpha");

            // Given
            String fakeActualResult = "{" +
                "\"something\": {" +
                "\"value\": 636," +
                "\"alphabet\":\"abcdefg\"," +
                "}" +
                "}";

            // When
            JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), fakeActualResult, expected);
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(Success);
        }

        @Test
        @DisplayName("matches")
        public void should_assert_with_matches_placeholder() {
            Map<String, Object> expected = new HashMap<>();
            expected.put("$.something.matchregexp", "$matches:\\d{4}-\\d{2}-\\d{2}");
            expected.put("$", "$matches:.*something=\\{alpha.*");

            // Given
            String fakeActualResult = "{" +
                "\"something\": {" +
                "\"alphabet\":\"abcdefg\"," +
                "\"matchregexp\":\"1983-10-26\"," +
                "}" +
                "}";

            // When
            JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), fakeActualResult, expected);
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(Success);
        }

        @Test
        @DisplayName("is[Before|Equal|After]Date")
        public void should_assert_with_comparison_date_placeholders() {
            Map<String, Object> expected = new HashMap<>();
            expected.put("$.something.onedate", "$isBeforeDate:2010-01-01T11:12:13.1230Z");
            expected.put("$.something.seconddate", "$isAfterDate:1998-07-14T02:03:04.456Z");
            expected.put("$.something.seconddate", "$isEqualDate:2000-01-01T11:11:12.123+01:00");

            // Given
            String fakeActualResult = "{" +
                "\"something\": {" +
                "\"onedate\":\"2000-01-01T11:11:12.123+01:00\"," +
                "\"seconddate\":\"2000-01-01T10:11:12.123Z\"," +
                "}" +
                "}";

            // When
            JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), fakeActualResult, expected);
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(Success);
        }

        @Test
        @DisplayName("is[Less|Greater]Than")
        public void should_assert_with_comparison_number_placeholders() {
            Map<String, Object> expected = new HashMap<>();
            expected.put("$.something.anumber", "$isLessThan:42000");
            expected.put("$.something.thenumber", "$isGreaterThan:45");

            // Given
            String fakeActualResult = "{" +
                "\"something\": {" +
                "\"anumber\":4 100," +
                "\"thenumber\":46," +
                "}" +
                "}";

            // When
            JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), fakeActualResult, expected);
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(Success);
        }

        @Test
        @DisplayName("value (json array)")
        public void should_assert_with_json_array_value_placeholder() {
            Map<String, Object> expected = new HashMap<>();
            expected.put("$.something.objectArray[?(@.name=='obj2')].array[0]", "$value:first");
            expected.put("$.something.objectArray[?(@.name=='obj2')].array", "$value:[\"first\",\"second\",\"three\"]");
            expected.put("$.something.objectArray[?(@.name=='obj1')].array[2]", "$value[0]:three");
            expected.put("$.something.objectArray[?(@.name=='obj3')].array", "$value:[]");

            // Given
            String fakeActualResult = "{" +
                "\"something\": {" +
                "\"objectArray\": [" +
                "{ \"name\": \"obj1\", \"array\": [ \"first\", \"second\", \"three\" ] }," +
                "{ \"name\": \"obj2\", \"array\": [ \"first\", \"second\", \"three\" ] }," +
                "{ \"name\": \"obj3\", \"array\": [ ] }" +
                "]," +
                "}" +
                "}";

            // When
            JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), fakeActualResult, expected);
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(Success);
        }

        @Test
        @DisplayName("isEmpty")
        public void should_assert_with_isEmpty_placeholder() {
            Map<String, Object> expected = new HashMap<>();
            expected.put("$.something.emptyArray", "$isEmpty");
            expected.put("$.something.emptyString", "$isEmpty");
            expected.put("$.something.objectArray[?(@.name=='obj3')].array", "$isEmpty");
            expected.put("$.something.objectArray[?(@.name=='obj4')].emptyString", "$isEmpty");

            // Given
            String fakeActualResult = "{" +
                "\"something\": {" +
                "\"objectArray\": [" +
                "{ \"name\": \"obj3\", \"array\": [ ] }" +
                "{ \"name\": \"obj4\", \"emptyString\": [ ] }" +
                "]," +
                "\"emptyArray\": []," +
                "\"emptyString\": \"\"" +
                "}" +
                "}";

            // When
            JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), fakeActualResult, expected);
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(Success);
        }

        @ParameterizedTest
        @MethodSource("lenientEqual")
        @DisplayName("lenientEqual")
        public void should_assert_with_lenientEqual_placeholder(String doc, String expectedString, TaskExecutionResult.Status expectedStatus) {
            Map<String, Object> expected = new HashMap<>();
            expected.put("$", "$lenientEqual:" + ofNullable(expectedString).orElse(doc));

            // When
            JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(), doc, expected);
            TaskExecutionResult result = jsonAssertTask.execute();

            // Then
            assertThat(result.status).isEqualTo(expectedStatus);
        }

        Stream<Arguments> lenientEqual() {
            return Stream.of(
                // Classic equals
                Arguments.of("{}", null, Success),
                Arguments.of("[1, \"value\"]", null, Success),
                Arguments.of("{\"string\": \"value\"}", null, Success),
                Arguments.of("{\"number\": 666}", null, Success),
                Arguments.of("{\"array\": [1, 2, 3]}", null, Success),
                Arguments.of("{\"object\":{\"string\":\"value\"}}", null, Success),

                // Extra attributes
                Arguments.of("{\"number\": 666}", "{}", Success),
                Arguments.of("{\"string\": \"val\"}", "{}", Success),
                Arguments.of("{\"array\": [666, \"val\", {\"att\": \"val\"}]}", "{}", Success),
                Arguments.of("{\"object\": {\"att\": \"val\"}}", "{}", Success),

                Arguments.of("{\"string\": \"val\", \"extra_number\": 666}", "{\"string\": \"val\"}", Success),
                Arguments.of("{\"string\": \"val\", \"extra_string\": \"value\"}", "{\"string\": \"val\"}", Success),
                Arguments.of("{\"string\": \"val\", \"extra_array\": [666, \"val\", {\"att\": \"val\"}]}", "{\"string\": \"val\"}", Success),
                Arguments.of("{\"string\": \"val\", \"extra_object\": {\"att\": \"val\"}}", "{\"string\": \"val\"}", Success),

                Arguments.of("{\"object\": {\"att\": \"val\", \"extra_att\": \"extra_val\"}}", "{\"object\": {\"att\": \"val\"}}", Success),

                Arguments.of("{\"string\": \"val\", \"extra_number_one\": 666}", "{\"string\": \"val\", \"extra_number_two\": 666}", Failure),
                Arguments.of("{\"string\": \"val\", \"object\": {\"att\": \"val\", \"extra_att_one\": \"val\"}}", "{\"string\": \"val\", \"object\": {\"att\": \"val\", \"extra_att_two\": \"val\"}}", Failure),

                // Array order
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
