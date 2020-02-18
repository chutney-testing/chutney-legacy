package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class JsonCompareTaskTest {

    @Test
    public void should_execute_3_successful_assertions_on_comparing_actual_result_to_expected() {
        Map<String, String> pathInputs = new HashMap<>();
        pathInputs.put("$.something.value", "$.something_else.value");
        pathInputs.put("$.a_thing", "$.a_thing");

        // Given
        String doc1 = "{\"something\":{\"value\":3},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"my_type\"}}";
        String doc2 = "{\"something_else\":{\"value\":3},\"a_thing\":{\"type\":\"my_type\"}}";

        // When
        JsonCompareTask jsonAssertTask = new JsonCompareTask(mock(Logger.class),
            doc1,
            doc2,
            pathInputs);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    public void should_failed_on_bad_assertions() {
        Map<String, String> pathInputs = new HashMap<>();
        pathInputs.put("$.something.value", "$.something.value");

        // Given
        String doc1 = "{\"something\":{\"value\":3},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"my_type\"}}";
        String doc2 = "{\"something\":{\"value\":4},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"my_type\"}}";

        // When
        JsonCompareTask jsonAssertTask = new JsonCompareTask(mock(Logger.class),
            doc1,
            doc2,
            pathInputs);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
    }
}
