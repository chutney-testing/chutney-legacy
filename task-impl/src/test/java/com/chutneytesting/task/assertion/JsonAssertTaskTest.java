package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class JsonAssertTaskTest {

    @Test
    public void should_take_zoned_date_when_asserting_dates() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.onedate", "$isBeforeDate:2020-08-14T15:07:46.621Z");
        expected.put("$.something.seconddate", "$isAfterDate:2020-08-14T16:56:56+02:00");

        // Given
        String fakeActualResult = "{" +
            "\"something\":{" +
            "\"onedate\":\"2020-08-14T16:56:56+02:00\"," +
            "\"seconddate\":\"2020-08-14T15:07:46.621Z\"" +
            "}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(),
            fakeActualResult,
            expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    public void should_execute_successful_assertions_with_placeholder() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", "$isNotNull");
        expected.put("$.something.notexist", "$isNull");
        expected.put("$.something.valuenull", "$isNull");
        expected.put("$.something.alphabet", "$contains:abcdefg");
        expected.put("$.something.matchregexp", "$matches:\\d{4}-\\d{2}-\\d{2}");
        expected.put("$.something.onedate", "$isBeforeDate:2010-01-01T11:12:13.1230Z");
        expected.put("$.something.seconddate", "$isAfterDate:1998-07-14T02:03:04.456Z");

        // Given
        String fakeActualResult = "{" +
            "\"something\":{" +
            "\"value\":3," +
            "\"alphabet\":\"abcdefg\"," +
            "\"valuenull\":null," +
            "\"matchregexp\":\"1983-10-26\"," +
            "\"onedate\":\"2000-01-01T10:11:12.123Z\"," +
            "\"seconddate\":\"2000-01-01T10:11:12.123Z\"" +
            "}" +
            "}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(new TestLogger(),
            fakeActualResult,
            expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    public void should_execute_4_successful_assertions_on_comparing_actual_result_to_expected() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", 3);
        expected.put("$.something_else.value", 5);
        expected.put("$.a_thing.type", "my_type");
        expected.put("$.a_thing.not.existing", null);

        // Given
        String fakeActualResult = "{\"something\":{\"value\":3},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"my_type\"}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class),
            fakeActualResult,
            expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    public void should_execute_a_failing_assertion_on_comparing_actual_result_to_expected() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", 42);

        // Given
        String fakeActualResult = "{\"something\":{\"value\":3}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class),
            fakeActualResult,
            expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @Test
    public void should_execute_a_failing_assertion_on_invalid_JSON_content_in_actual() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", 1);

        // Given
        String fakeInvalidJson = "{\"EXCEPTION 42 - BSOD\"}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class),
            fakeInvalidJson,
            expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @Test
    public void should_execute_a_failing_assertion_on_wrong_XPath_value_in_expected() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.wrong.json.x.xpath", 1);

        // Given
        String fakeActualResult = "{\"xpath\":{\"to\":\"value\"}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class),
            fakeActualResult,
            expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }

    @Test
    public void should_not_convert_int_as_long() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", 400.0);

        // Given
        String fakeActualResult = "{\"something\":{\"value\":400.0}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class),
            fakeActualResult,
            expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    public void should_execute_a_successful_assertions_on_comparing_expected_value_as_string_and_actual_value_as_number() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("$.something.value", "my_value");

        // Given
        String fakeActualResult = "{\"something\":{\"value\": my_value}}";

        // When
        JsonAssertTask jsonAssertTask = new JsonAssertTask(mock(Logger.class),
            fakeActualResult,
            expected);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }
}
