package com.chutneytesting.engine.domain.execution.strategies;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import org.junit.jupiter.api.Test;

public class SoftAssertStrategyTest {

    private final StepExecutionStrategy sut = new SoftAssertStrategy();

    @Test
    public void should_run_all_substeps_regardless_of_failure_when_using_soft_assert_on_parent_step() {
        // Given
        ScenarioExecution scenarioExecution = null;

        Step failureStep = mock(Step.class);
        when(failureStep.execute(any(), any())).thenReturn(Status.FAILURE);
        Step failureStep2 = mock(Step.class);
        when(failureStep2.execute(any(), any())).thenReturn(Status.FAILURE);
        Step successStep = mock(Step.class);
        when(successStep.execute(any(), any())).thenReturn(Status.SUCCESS);

        Step rootStep = mock(Step.class);
        when(rootStep.subSteps()).thenReturn(newArrayList(failureStep, failureStep2, successStep));
        when(rootStep.isParentStep()).thenReturn(true);

        ScenarioContext scenarioContext = null;
        StepExecutionStrategies strategies = mock(StepExecutionStrategies.class);
        when(strategies.buildStrategyFrom(any())).thenReturn(DefaultStepExecutionStrategy.instance);

        // When
        Status actualStatus = sut.execute(scenarioExecution, rootStep, scenarioContext, strategies);

        // Then
        verify(failureStep, times(1)).execute(any(), any());
        verify(failureStep2, times(1)).execute(any(), any());
        verify(successStep, times(1)).execute(any(), any());
        assertThat(actualStatus).isEqualTo(Status.WARN);
    }

    @Test
    public void should_run_next_step_when_current_step_fails_with_soft_assert() {
        // Given
        ScenarioExecution scenarioExecution = null;

        Step failureStepWithSoftAssert = mock(Step.class);
        when(failureStepWithSoftAssert.execute(any(), any())).thenReturn(Status.FAILURE);

        Step nextStep = mock(Step.class);
        when(nextStep.execute(any(), any())).thenReturn(Status.SUCCESS);

        Step rootStep = mock(Step.class);
        when(rootStep.subSteps()).thenReturn(newArrayList(failureStepWithSoftAssert, nextStep));
        when(rootStep.isParentStep()).thenReturn(true);

        ScenarioContext scenarioContext = null;
        StepExecutionStrategies strategies = mock(StepExecutionStrategies.class);
        when(strategies.buildStrategyFrom(rootStep)).thenReturn(DefaultStepExecutionStrategy.instance);
        when(strategies.buildStrategyFrom(failureStepWithSoftAssert)).thenReturn(sut);
        when(strategies.buildStrategyFrom(nextStep)).thenReturn(DefaultStepExecutionStrategy.instance);

        // When
        DefaultStepExecutionStrategy.instance.execute(scenarioExecution, rootStep, scenarioContext, strategies);

        // Then
        verify(failureStepWithSoftAssert, times(1)).execute(any(), any());
        verify(nextStep, times(1)).execute(any(), any());
    }

}
