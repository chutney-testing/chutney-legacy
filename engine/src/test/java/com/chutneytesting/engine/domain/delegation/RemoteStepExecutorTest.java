package com.chutneytesting.engine.domain.delegation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.engine.step.StepContext;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReportBuilder;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;

public class RemoteStepExecutorTest {

    @Test
    public void should_call_next_agent_and_update_context_and_information_from_report() {

        // Given
        StepExecutionReport fakeRemoteReport = new StepExecutionReportBuilder().setName("name")
            .setStartDate(Instant.now())
            .setStatus(Status.SUCCESS)
            .setEvaluatedInputs(Collections.emptyMap())
            .setScenarioContext(Collections.emptyMap())
            .setStepResults(Collections.emptyMap())
            .createStepExecutionReport();

        DelegationClient mockHttpClient = mock(DelegationClient.class);
        StepContext spyCurrentStepContext = spy(StepContext.class);
        Step step = new Step(mock(StepDataEvaluator.class), mock(StepDefinition.class), Optional.empty(),mock(StepExecutor.class), Collections.EMPTY_LIST);
        Step spyCurrentStep = spy(step);
        NamedHostAndPort mockDelegate = mock(NamedHostAndPort.class);
        when(mockHttpClient.handDown(any(), any()))
            .thenReturn(fakeRemoteReport);

        // When
        RemoteStepExecutor remoteStepExecutor = new RemoteStepExecutor(mockHttpClient, mockDelegate);
        remoteStepExecutor.execute(null, spyCurrentStepContext, mock(Target.class), spyCurrentStep);

        // Then
        verify(spyCurrentStepContext, times(1)).addScenarioContext(fakeRemoteReport.scenarioContext);
        verify(spyCurrentStepContext, times(1)).addStepOutputs(fakeRemoteReport.stepResults);

        verify(spyCurrentStep, times(1)).addInformation(
            (String[]) Lists.newArrayList(fakeRemoteReport.information).toArray(new String[fakeRemoteReport.information.size()])
        );

        verify(spyCurrentStep, times(0)).failure(
            (String[]) Lists.newArrayList(fakeRemoteReport.errors).toArray(new String[fakeRemoteReport.errors.size()])
        );
    }

    @Test
    public void should_throw_if_remote_context_is_null() {

        // Given
        StepContext mockCurrentStepContext = mock(StepContext.class);
        StepExecutionReport mockStepExecutionReport = mock(StepExecutionReport.class);
        DelegationClient mockHttpClient = mock(DelegationClient.class);
        NamedHostAndPort mockDelegate = mock(NamedHostAndPort.class);

        when(mockHttpClient.handDown(any(), any()))
            .thenReturn(mockStepExecutionReport);

        RemoteStepExecutor remoteStepExecutor = new RemoteStepExecutor(mockHttpClient, mockDelegate);

        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> /* When */ remoteStepExecutor.execute(null, mockCurrentStepContext, mock(Target.class), mock(Step.class)));
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
        StepContext spyCurrentStepContext = spy(StepContext.class);
        Step step = new Step(mock(StepDataEvaluator.class), mock(StepDefinition.class), Optional.empty(),mock(StepExecutor.class), Collections.EMPTY_LIST);
        Step spyCurrentStep = spy(step);
        NamedHostAndPort mockDelegate = mock(NamedHostAndPort.class);
        when(mockHttpClient.handDown(any(), any()))
            .thenReturn(fakeRemoteReport);

        // When
        RemoteStepExecutor remoteStepExecutor = new RemoteStepExecutor(mockHttpClient, mockDelegate);
        remoteStepExecutor.execute(null, spyCurrentStepContext, mock(Target.class), spyCurrentStep);

        // Then
        verify(spyCurrentStepContext, times(1)).addScenarioContext(fakeRemoteReport.scenarioContext);
        verify(spyCurrentStepContext, times(1)).addStepOutputs(fakeRemoteReport.stepResults);

        verify(spyCurrentStep, times(1)).addInformation(
            (String[]) Lists.newArrayList(fakeRemoteReport.information).toArray(new String[fakeRemoteReport.information.size()])
        );

        verify(spyCurrentStep, times(1)).failure(
            (String[]) Lists.newArrayList(fakeRemoteReport.errors).toArray(new String[fakeRemoteReport.errors.size()])
        );
    }

}
