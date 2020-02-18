package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class AssertTaskTest {

    public static Object[] parametersForShould_success_when_true_and_failed_when_false() {
        return new Object[]{
            new Object[]{true, Success},
            new Object[]{false, Failure}
        };
    }

    @Test
    @Parameters
    public void should_success_when_true_and_failed_when_false(Boolean value, TaskExecutionResult.Status expected) {
        // Given
        List<Map<String, Boolean>> assertions = new ArrayList<>();
        Map<String, Boolean> assertion = new HashMap<>();
        assertion.put("assert-true", value);
        assertions.add(assertion);

        // When
        AssertTask assertTask = new AssertTask(mock(Logger.class), assertions);
        TaskExecutionResult result = assertTask.execute();

        // Then
        Assertions.assertThat(result.status).isEqualTo(expected);
    }
}

