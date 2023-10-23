package com.chutneytesting.engine.domain.execution.strategies;

import static com.chutneytesting.engine.domain.execution.ScenarioExecution.createScenarioExecution;
import static com.chutneytesting.engine.domain.execution.report.Status.FAILURE;
import static com.chutneytesting.engine.domain.execution.report.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IfStrategyTest {

    static Stream<Arguments> parametrizedIfStrategyTest() {
        return Stream.of(
            of("Should not execute step if condition false", false, 0, SUCCESS, null),
            of("Should execute step if condition true", true, 1, SUCCESS, null, null),
            of("Should not execute step if condition false Spel", "${ (1+1) == 3}", 0, SUCCESS, null),
            of("Should execute step if condition true Spel", "${ (1+1) == 2}", 1, SUCCESS, null),
            of("Should return error if spel does not return a Boolan", "i am not a boolean", 1, FAILURE, "Error message: class java.lang.String cannot be cast to class java.lang.Boolean (java.lang.String and java.lang.Boolean are in module java.base of loader 'bootstrap')"),
            of("should return error if spel cannot be evaluated", "${'test'.notExistingMethod}", 1, FAILURE, "Error message: Cannot resolve 'test'.notExistingMethod , EL1008E: Property or field 'notExistingMethod' cannot be found on object of type 'java.lang.String' - maybe not public or not valid?")
        );
    }

    @ParameterizedTest(name = "#{index}: {0}")
    @MethodSource("parametrizedIfStrategyTest")
    void if_strategy_parametrized_test(String testName, Object ifCondition, Integer stepExecutionNumber, Status expectedStatus, String errorMessage) {

        //Given
        StrategyProperties strategyProperties = new StrategyProperties();
        strategyProperties.put("condition", ifCondition);
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("if", strategyProperties);

        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));
        when(step.execute(any(), any(), any())).thenReturn(SUCCESS);
        StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
        when(step.dataEvaluator()).thenReturn(dataEvaluator);
        StepExecutionStrategy ifStrategy = new IfStrategy();

        if (errorMessage == null) {
            // When
            Status status = ifStrategy.execute(createScenarioExecution(null), step, new ScenarioContextImpl(), null);

            //Then
            verify(step, times(stepExecutionNumber)).execute(any(), any(), any());
            assertThat(status).isEqualTo(expectedStatus);
            verify(step, times(1)).addInformation("Execution condition [" + ifCondition + "] = " + (stepExecutionNumber == 1 ? "step executed" : "step skipped"));
            if (stepExecutionNumber == 0) {
                verify(step, times(1)).success();
            }
        } else {
            // When + Then
            assertThatThrownBy(() -> ifStrategy.execute(createScenarioExecution(null), step, new ScenarioContextImpl(), null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot evaluate execution condition: [" + ifCondition + "]. " + errorMessage);
        }
    }

    @Test
    void should_throw_error_if_no_strategy_definition() {
        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(Optional.empty());

        StepExecutionStrategy ifStrategy = new IfStrategy();
        assertThatThrownBy(() -> ifStrategy.execute(createScenarioExecution(null), step, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Strategy definition cannot be empty");
    }

    @Test
    void should_throw_error_if_condition_property() {
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("if", new StrategyProperties());

        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));

        StepExecutionStrategy ifStrategy = new IfStrategy();
        assertThatThrownBy(() -> ifStrategy.execute(createScenarioExecution(null), step, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Property [condition] mandatory for if strategy");
    }
}
