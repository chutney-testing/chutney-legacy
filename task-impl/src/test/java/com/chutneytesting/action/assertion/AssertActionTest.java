package com.chutneytesting.action.assertion;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Failure;
import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static org.mockito.Mockito.mock;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AssertActionTest {

    public static Object[] parametersForShould_success_when_true_and_failed_when_false() {
        return new Object[]{
            new Object[]{true, Success},
            new Object[]{false, Failure}
        };
    }

    @ParameterizedTest
    @MethodSource("parametersForShould_success_when_true_and_failed_when_false")
    public void should_success_when_true_and_failed_when_false(Boolean value, ActionExecutionResult.Status expected) {
        // Given
        List<Map<String, Boolean>> assertions = new ArrayList<>();
        Map<String, Boolean> assertion = new HashMap<>();
        assertion.put("assert-true", value);
        assertions.add(assertion);

        // When
        AssertAction assertAction = new AssertAction(mock(Logger.class), assertions);
        ActionExecutionResult result = assertAction.execute();

        // Then
        Assertions.assertThat(result.status).isEqualTo(expected);
    }
}

