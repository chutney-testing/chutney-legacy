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

package com.chutneytesting.action.assertion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class CompareActionTest {

    Logger logger = mock(Logger.class);

    @Test
    public void should_fail_and_log_error_when_wrong_mode_is_entered() {

        // Given
        String actual = "boo";
        String expected = "boo";
        String mode = "wrong mode";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "Sorry, this mode is not existed in our mode list, please refer to documentation to check it.";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_equals_compare_action_success() {

        // Given
        String actual = "boo";
        String expected = "boo";
        String mode = "EQUALS";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + expected + "]" + " EQUALS " + "[" + actual + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_equals_compare_action_failure() {

        // Given
        String actual = "boo";
        String expected = "oob";
        String mode = "equals";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + expected + "]" + " NOT EQUALS " + "[" + actual + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_not_equals_compare_action_success() {

        // Given
        String actual = "boo";
        String expected = "oob";
        String mode = "not-equals";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + expected + "]" + " NOT EQUALS " + "[" + actual + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_not_equals_compare_action_failure() {

        // Given
        String actual = "boo";
        String expected = "boo";
        String mode = "not equals";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + expected + "]" + " EQUALS " + "[" + actual + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_contains_compare_action_success() {

        // Given
        String actual = "boo";
        String expected = "o";
        String mode = "contains";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "]" + " CONTAINS " + "[" + expected + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_contains_compare_action_failure() {

        // Given
        String actual = "boo";
        String expected = "a";
        String mode = "contains";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "]" + " NOT CONTAINS " + "[" + expected + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_not_contains_compare_action_success() {

        // Given
        String actual = "boo";
        String expected = "a";
        String mode = "not contains";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "] NOT CONTAINS [" + expected + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_not_contains_compare_action_failure() {

        // Given
        String actual = "boo";
        String expected = "b";
        String mode = "not contains";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "]" + " CONTAINS " + "[" + expected + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_greater_than_compare_action_success() {

        // Given
        String actual = "10000";
        String expected = "2000";
        String mode = "greater than";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "] IS GREATER THAN [" + expected + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Success);
    }

    @Test
    public void should_fail_and_log_error_when_word_is_entered_in_greater_than_compare_action() {

        // Given
        String actual = "10000";
        String expected = "boo";
        String mode = "greater than";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + expected + "] is Not Numeric";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_greater_than_compare_action_failure() {

        // Given
        String actual = "1";
        String expected = "2";
        String mode = "greater than";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "] IS LESS THAN [" + expected + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    public void should_perform_less_than_compare_action_success() {

        // Given
        String actual = "1234";
        String expected = "2345";
        String mode = "less than";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "] IS LESS THAN [" + expected + "]";
        verify(logger).info(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Success);
    }

    @Test
    public void should_perform_less_than_compare_action_failure() {

        // Given
        String actual = "200000000000";
        String expected = "12222";
        String mode = "less than";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "] IS GREATER THAN [" + expected + "]";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    public void should_fail_and_log_error_when_date_is_entered_in_greater_than_compare_action() {

        // Given
        String actual = Instant.now().toString();
        String expected = Instant.now().minusSeconds(60).toString();
        String mode = "greater than";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "] is Not Numeric";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }


    @Test
    public void should_fail_and_log_error_when_date_is_entered_in_less_than_compare_action() {

        // Given
        String actual = Instant.now().toString();
        String expected = Instant.now().minusSeconds(60).toString();
        String mode ="less than";
        CompareAction compareAction = new CompareAction(logger, actual, expected, mode);

        // When
        ActionExecutionResult result = compareAction.execute();

        // Then
        String logExpected =
            "[" + actual + "] is Not Numeric";
        verify(logger).error(logExpected);
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }
}
