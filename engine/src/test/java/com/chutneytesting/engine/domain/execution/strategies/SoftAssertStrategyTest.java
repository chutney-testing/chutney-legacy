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

package com.chutneytesting.engine.domain.execution.strategies;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import org.junit.jupiter.api.Test;

public class SoftAssertStrategyTest {

    private final StepExecutionStrategy sut = new SoftAssertStrategy();

    @Test
    public void should_run_all_substeps_regardless_of_failure_when_using_soft_assert_on_parent_step() {
        // Given
        Step failureStep = mock(Step.class);
        when(failureStep.execute(any(), any(), any())).thenReturn(Status.FAILURE);
        Step failureStep2 = mock(Step.class);
        when(failureStep2.execute(any(), any(), any())).thenThrow(new RuntimeException());
        Step successStep = mock(Step.class);
        when(successStep.execute(any(), any(), any())).thenReturn(Status.SUCCESS);

        Step rootStep = mock(Step.class);
        when(rootStep.subSteps()).thenReturn(newArrayList(failureStep, failureStep2, successStep));
        when(rootStep.isParentStep()).thenReturn(true);

        StepExecutionStrategies strategies = mock(StepExecutionStrategies.class);
        when(strategies.buildStrategyFrom(any())).thenReturn(DefaultStepExecutionStrategy.instance);

        // When
        Status actualStatus = sut.execute(null, rootStep, null, strategies);

        // Then
        verify(failureStep).execute(any(), any(), any());
        verify(failureStep2).execute(any(), any(), any());
        verify(successStep).execute(any(), any(), any());
        assertThat(actualStatus).isEqualTo(Status.WARN);
    }

    @Test
    public void should_run_next_step_when_current_step_fails_with_soft_assert() {
        // Given
        Step failureStepWithSoftAssert = mock(Step.class);
        when(failureStepWithSoftAssert.execute(any(), any())).thenReturn(Status.FAILURE);

        Step nextStep = mock(Step.class);
        when(nextStep.execute(any(), any())).thenReturn(Status.SUCCESS);

        Step rootStep = mock(Step.class);
        when(rootStep.subSteps()).thenReturn(newArrayList(failureStepWithSoftAssert, nextStep));
        when(rootStep.isParentStep()).thenReturn(true);

        StepExecutionStrategies strategies = mock(StepExecutionStrategies.class);
        when(strategies.buildStrategyFrom(rootStep)).thenReturn(DefaultStepExecutionStrategy.instance);
        when(strategies.buildStrategyFrom(failureStepWithSoftAssert)).thenReturn(sut);
        when(strategies.buildStrategyFrom(nextStep)).thenReturn(DefaultStepExecutionStrategy.instance);

        // When
        DefaultStepExecutionStrategy.instance.execute(null, rootStep, null, strategies);

        // Then
        verify(failureStepWithSoftAssert).execute(any(), any(), any());
        verify(nextStep).execute(any(), any(), any());
    }

}
