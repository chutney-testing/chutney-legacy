package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JsonAssertTaskTest {

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
