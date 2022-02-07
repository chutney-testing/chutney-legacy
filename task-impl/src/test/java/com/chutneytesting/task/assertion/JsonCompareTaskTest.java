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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
}
