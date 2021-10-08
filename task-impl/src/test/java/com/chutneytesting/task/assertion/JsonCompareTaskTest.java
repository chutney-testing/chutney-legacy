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
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JsonCompareTaskTest {

    @Test
    public void should_execute_2_successful_assertions_on_comparing_actual_result_to_expected() {
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
        // Given
        Map<String, String> pathInputs = new HashMap<>();
        pathInputs.put("$.something.value", "$.something.value");//ko
        pathInputs.put("$.something_else.value", "$.something_else.value");//ok
        pathInputs.put("$.a_thing.type", "$.a_thing.type");//ko

        String doc1 = "{\"something\":{\"value\":3},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"type\"}}";
        String doc2 = "{\"something\":{\"value\":4},\"something_else\":{\"value\":5},\"a_thing\":{\"type\":\"another_type\"}}";

        Logger mockLogger = mock(Logger.class);
        // When
        JsonCompareTask jsonAssertTask = new JsonCompareTask(
            mockLogger,
            doc1,
            doc2,
            pathInputs);
        TaskExecutionResult result = jsonAssertTask.execute();

        // Then
        assertThat(result.status).isEqualTo(Failure);
        verify(mockLogger, times(2)).error(anyString());
    }
}
