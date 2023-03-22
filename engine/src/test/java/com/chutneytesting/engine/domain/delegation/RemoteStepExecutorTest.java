package com.chutneytesting.engine.domain.delegation;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReportBuilder;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class RemoteStepExecutorTest {

    @Test
    public void should_call_next_agent_and_update_step_from_report() {

        // Given
        StepExecutionReport fakeRemoteReport = new StepExecutionReportBuilder().setName("name")
            .setStartDate(Instant.now())
            .setStatus(Status.SUCCESS)
            .setEvaluatedInputs(Collections.emptyMap())
            .setScenarioContext(Collections.emptyMap())
            .setStepResults(Collections.emptyMap())
            .createStepExecutionReport();

        DelegationClient mockHttpClient = mock(DelegationClient.class);
        Step mockStep = mock(Step.class);
        NamedHostAndPort mockDelegate = mock(NamedHostAndPort.class);
        when(mockHttpClient.handDown(any(), any()))
            .thenReturn(fakeRemoteReport);

        // When
        RemoteStepExecutor remoteStepExecutor = new RemoteStepExecutor(mockHttpClient, mockDelegate);
        remoteStepExecutor.execute(null, mock(TargetImpl.class), mockStep);

        // Then
        verify(mockStep, times(1)).updateContextFrom(fakeRemoteReport);
    }

    @Test
    public void should_throw_if_remote_context_is_null() {

        // Given
        StepExecutionReport mockStepExecutionReport = mock(StepExecutionReport.class);
        DelegationClient mockHttpClient = mock(DelegationClient.class);
        NamedHostAndPort mockDelegate = mock(NamedHostAndPort.class);

        when(mockHttpClient.handDown(any(), any()))
            .thenReturn(mockStepExecutionReport);

        RemoteStepExecutor remoteStepExecutor = new RemoteStepExecutor(mockHttpClient, mockDelegate);

        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> /* When */ remoteStepExecutor.execute(null, mock(TargetImpl.class), mock(Step.class)));
    }

    @Test
    public void should_propagate_failures_when_report_status_is_KO() {

        // Given
        StepExecutionReport fakeRemoteReport = new StepExecutionReportBuilder().setName("name")
            .setStartDate(Instant.now())
            .setStatus(Status.FAILURE)
            .setEvaluatedInputs(Collections.emptyMap())
            .setScenarioContext(Collections.emptyMap())
            .setStepResults(Collections.emptyMap())
            .createStepExecutionReport();

        DelegationClient mockHttpClient = mock(DelegationClient.class);
        Step step = new Step(mock(StepDataEvaluator.class), mock(StepDefinition.class), mock(StepExecutor.class), emptyList());
        Step spyCurrentStep = spy(step);
        NamedHostAndPort mockDelegate = mock(NamedHostAndPort.class);
        when(mockHttpClient.handDown(any(), any()))
            .thenReturn(fakeRemoteReport);

        // When
        RemoteStepExecutor remoteStepExecutor = new RemoteStepExecutor(mockHttpClient, mockDelegate);
        remoteStepExecutor.execute(null, mock(TargetImpl.class), spyCurrentStep);

        // Then
        verify(spyCurrentStep, times(1)).updateContextFrom(fakeRemoteReport);
        verify(spyCurrentStep, times(1)).failure(
            Lists.newArrayList(fakeRemoteReport.errors).toArray(new String[fakeRemoteReport.errors.size()])
        );
    }

}
