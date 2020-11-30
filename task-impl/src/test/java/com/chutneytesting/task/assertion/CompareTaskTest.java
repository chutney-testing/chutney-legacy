package com.chutneytesting.task.assertion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class CompareTaskTest {

    Logger logger = mock(Logger.class);

    @Test
    public void should_fail_and_log_error_when_wrong_mode_is_entered() {

        // Given
        String actual = "boo";
        String expected = "boo";
        String mode = "wrong mode";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "Sorry, this mode is not existed in our mode list, please refer to documentation to check it.";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_equals_compare_task_success() {

        // Given
        String actual = "boo";
        String expected = "boo";
        String mode = "EQUALS";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + expected + "]" + " EQUALS " + "[" + actual + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_equals_compare_task_failure() {

        // Given
        String actual = "boo";
        String expected = "oob";
        String mode = "equals";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + expected + "]" + " NOT EQUALS " + "[" + actual + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_not_equals_compare_task_success() {

        // Given
        String actual = "boo";
        String expected = "oob";
        String mode = "not-equals";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + expected + "]" + " NOT EQUALS " + "[" + actual + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_not_equals_compare_task_failure() {

        // Given
        String actual = "boo";
        String expected = "boo";
        String mode = "not equals";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + expected + "]" + " EQUALS " + "[" + actual + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_contains_compare_task_success() {

        // Given
        String actual = "boo";
        String expected = "o";
        String mode = "contains";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "]" + " CONTAINS " + "[" + expected + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_contains_compare_task_failure() {

        // Given
        String actual = "boo";
        String expected = "a";
        String mode = "contains";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "]" + " NOT CONTAINS " + "[" + expected + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_not_contains_compare_task_success() {

        // Given
        String actual = "boo";
        String expected = "a";
        String mode = "not contains";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "] NOT CONTAINS [" + expected + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_not_contains_compare_task_failure() {

        // Given
        String actual = "boo";
        String expected = "b";
        String mode = "not contains";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "]" + " CONTAINS " + "[" + expected + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_greater_than_compare_task_success() {

        // Given
        String actual = "10000";
        String expected = "2000";
        String mode = "greater than";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "] IS GREATER THAN [" + expected + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
    }

    @Test
    public void should_fail_and_log_error_when_word_is_entered_in_greater_than_compare_task() {

        // Given
        String actual = "10000";
        String expected = "boo";
        String mode = "greater than";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + expected + "] is Not Numeric";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_greater_than_compare_task_failure() {

        // Given
        String actual = "1";
        String expected = "2";
        String mode = "greater than";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "] IS LESS THAN [" + expected + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_less_than_compare_task_success() {

        // Given
        String actual = "1234";
        String expected = "2345";
        String mode = "less than";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "] IS LESS THAN [" + expected + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_less_than_compare_task_failure() {

        // Given
        String actual = "200000000000";
        String expected = "12222";
        String mode = "less than";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "] IS GREATER THAN [" + expected + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    public void should_fail_and_log_error_when_date_is_entered_in_greater_than_compare_task() {

        // Given
        String actual = Instant.now().toString();
        String expected = Instant.now().minusSeconds(60).toString();
        String mode = "greater than";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "] is Not Numeric";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }


    @Test
    public void should_fail_and_log_error_when_date_is_entered_in_less_than_compare_task() {

        // Given
        String actual = Instant.now().toString();
        String expected = Instant.now().minusSeconds(60).toString();
        String mode ="less than";
        CompareTask compareTask = new CompareTask(logger, actual, expected, mode);

        // When
        TaskExecutionResult result = compareTask.execute();

        // Then
        String logExpected =
            "[" + actual + "] is Not Numeric";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }
}
