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


package com.chutneytesting.server.core.domain.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.processor.TestCasePreProcessors;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCoreBuilder;
import com.chutneytesting.server.core.domain.execution.state.ExecutionStateRepository;
import com.chutneytesting.server.core.domain.instrument.ChutneyMetrics;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ScenarioExecutionEngineAsyncTest {

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();
    private final ExecutionHistoryRepository executionHistoryRepository = mock(ExecutionHistoryRepository.class);
    private final ServerTestEngine executionEngine = mock(ServerTestEngine.class);
    private final ExecutionStateRepository executionStateRepository = mock(ExecutionStateRepository.class);
    private final ChutneyMetrics metrics = mock(ChutneyMetrics.class);
    private final TestCasePreProcessors testCasePreProcessors = mock(TestCasePreProcessors.class);

    @AfterEach
    public void after() {
        RxJavaPlugins.reset();
    }

    @Test
    public void should_not_follow_not_existing_scenario_execution() {
        final String scenarioId = "1";
        final ScenarioExecutionEngineAsync sut = new ScenarioExecutionEngineAsync(
            executionHistoryRepository,
            executionEngine,
            executionStateRepository,
            metrics,
            testCasePreProcessors,
            om
        );

        // When / Then
        assertThatThrownBy(() -> sut.followExecution(scenarioId, 2L))
            .isInstanceOf(ScenarioNotRunningException.class)
            .hasMessageContaining(scenarioId);
    }

    @Test
    public void should_store_initial_report_and_notify_start_end_execution_and_metrics_when_execute_empty_scenario() {
        // Given
        final TestCase testCase = emptyTestCase();
        final String scenarioId = testCase.id();
        final long executionId = 3L;

        when(testCasePreProcessors.apply(any())).thenReturn(testCase);
        when(executionEngine.executeAndFollow(any())).thenReturn(Pair.of(Observable.empty(), 0L));

        ExecutionHistory.Execution storedExecution = stubHistoryExecution(scenarioId, executionId);

        final ScenarioExecutionEngineAsync sut = new ScenarioExecutionEngineAsync(
            executionHistoryRepository,
            executionEngine,
            executionStateRepository,
            metrics,
            testCasePreProcessors,
            om,
            0,
            0
        );

        // When
        ExecutionRequest request = new ExecutionRequest(testCase, "Exec env", "Exec user");
        sut.execute(request);

        // Then
        verify(testCasePreProcessors).apply(request);
        verify(executionEngine).executeAndFollow(any());
        ArgumentCaptor<ExecutionHistory.DetachedExecution> argumentCaptor = ArgumentCaptor.forClass(ExecutionHistory.DetachedExecution.class);
        verify(executionHistoryRepository).store(eq(scenarioId), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().environment()).isEqualTo("Exec env");
        assertThat(argumentCaptor.getValue().user()).isEqualTo("Exec user");

        // Wait for background computation
        verify(executionStateRepository, timeout(250)).notifyExecutionStart(scenarioId);
        verify(executionStateRepository, timeout(250)).notifyExecutionEnd(scenarioId);
        verify(metrics, timeout(250)).onScenarioExecutionEnded(testCase, storedExecution);
    }

    @Test
    public void should_send_reports_and_update_history_and_send_metrics_and_notify_startend_when_observe_engine_execution() {
        // Given
        final TestCase testCase = emptyTestCase();
        final String scenarioId = testCase.id();
        final Long executionId = 4L;

        stubHistoryExecution(scenarioId, executionId);
        final Triple<Pair<Observable<StepExecutionReportCore>, Long>, List<StepExecutionReportCore>, TestScheduler> engineStub = stubEngineExecution(100);
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> engineStub.getRight());

        final ScenarioExecutionEngineAsync sut = new ScenarioExecutionEngineAsync(
            executionHistoryRepository,
            executionEngine,
            executionStateRepository,
            metrics,
            testCasePreProcessors,
            om
        );
        sut.setRetentionDelaySeconds(1);
        sut.setDebounceMilliSeconds(0);

        // When
        TestObserver<ScenarioExecutionReport> testObserver = sut.buildScenarioExecutionReportObservable(new ExecutionRequest(emptyTestCase(), "", ""), executionId, engineStub.getLeft()).test();

        // Then
        assertTestObserverStateWithValues(testObserver, 0, false);
        verify(executionStateRepository).notifyExecutionStart(scenarioId);

        engineStub.getRight().advanceTimeBy(100, TimeUnit.MILLISECONDS); // Reach first emission
        //assertTestObserverStateAndValues(testObserver, executionId, engineStub.getMiddle(), 1);
        verify(executionHistoryRepository).update(eq(scenarioId), any());

        engineStub.getRight().advanceTimeBy(100, TimeUnit.MILLISECONDS); // Reach second emission
        assertTestObserverStateAndValues(testObserver, executionId, engineStub.getMiddle(), 2);
        verify(executionHistoryRepository, times(2)).update(eq(scenarioId), any());

        engineStub.getRight().advanceTimeBy(100, TimeUnit.MILLISECONDS); // Reach third emission
        assertTestObserverStateAndValues(testObserver, executionId, engineStub.getMiddle(), 3);
        verify(executionHistoryRepository, times(3)).update(eq(scenarioId), any());

        engineStub.getRight().advanceTimeBy(100, TimeUnit.MILLISECONDS); // Reach last emission
        assertTestObserverStateAndValues(testObserver, true, executionId, engineStub.getMiddle(), 4);
        verify(executionHistoryRepository, times(4)).update(eq(scenarioId), any());

        verify(executionStateRepository).notifyExecutionEnd(scenarioId);
        verify(metrics).onScenarioExecutionEnded(any(), any());

        testObserver.dispose();
    }

    @Test
    public void should_observe_reports_when_follow_execution() {
        // Given
        final String scenarioId = "1";
        final long executionId = 5L;
        final TestCase testCase = emptyTestCase();

        when(executionStateRepository.runningState(scenarioId)).thenReturn(Optional.empty());
        when(testCasePreProcessors.apply(any())).thenReturn(testCase);

        stubHistoryExecution(scenarioId, executionId);
        Triple<Pair<Observable<StepExecutionReportCore>, Long>, List<StepExecutionReportCore>, TestScheduler> engineStub = stubEngineExecution(100);
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> engineStub.getRight());
        final List<StepExecutionReportCore> reportsList = engineStub.getMiddle();

        final ScenarioExecutionEngineAsync sut = new ScenarioExecutionEngineAsync(
            executionHistoryRepository,
            executionEngine,
            executionStateRepository,
            metrics,
            testCasePreProcessors,
            om,
            10,
            0
        );

        // When
        ExecutionRequest request = new ExecutionRequest(testCase, "", "");
        Long executionIdFromExecute = sut.execute(request);
        TestObserver<ScenarioExecutionReport> testObserver = sut.followExecution(testCase.id(), executionIdFromExecute).test();

        // Then
        engineStub.getRight().advanceTimeBy(500, TimeUnit.MILLISECONDS);
        assertTestObserverStateAndValues(testObserver, true, executionId, reportsList, 4);

        testObserver.dispose();
    }

    @Test
    public void should_save_not_executed_scenario_report() {
        // Given
        final ScenarioExecutionEngineAsync sut = new ScenarioExecutionEngineAsync(
            executionHistoryRepository,
            executionEngine,
            executionStateRepository,
            metrics,
            testCasePreProcessors,
            om
        );
        TestCase testCase = emptyTestCase();
        ExecutionRequest executionRequest = new ExecutionRequest(testCase, "env", "userId");

        when(executionHistoryRepository.store(eq("1"), any()))
            .thenReturn(mock(ExecutionHistory.Execution.class));

        // When
        sut.saveNotExecutedScenarioExecution(executionRequest);

        // Then
        verify(executionHistoryRepository, times(1)).update(eq("1"), any(ExecutionHistory.Execution.class));
    }

    @Test
    public void should_save_not_executed_scenario_execution() {
        // Given
        final ScenarioExecutionEngineAsync sut = new ScenarioExecutionEngineAsync(
            executionHistoryRepository,
            executionEngine,
            executionStateRepository,
            metrics,
            testCasePreProcessors,
            om
        );
        TestCase testCase = emptyTestCase();
        ExecutionRequest executionRequest = new ExecutionRequest(testCase, "env", "userId");

        ExecutionHistory.Execution expected = ImmutableExecutionHistory.Execution.builder()
            .executionId(1L)
            .time(LocalDateTime.now())
            .duration(0L)
            .status(ServerReportStatus.NOT_EXECUTED)
            .testCaseTitle(executionRequest.testCase.metadata().title())
            .environment(executionRequest.environment)
            .user(executionRequest.userId)
            .report("")
            .build();
        when(executionHistoryRepository.store(eq("1"), any(ExecutionHistory.DetachedExecution.class))).thenReturn(expected);

        // When
        ExecutionHistory.Execution execution = sut.saveNotExecutedScenarioExecution(executionRequest);

        // Then
        verify(executionHistoryRepository, times(1)).store(eq("1"), any(ExecutionHistory.DetachedExecution.class));
        assertThat(execution).isEqualTo(expected);
    }

    private void assertTestObserverStateAndValues(TestObserver<ScenarioExecutionReport> testObserver, Long executionId, List<StepExecutionReportCore> reportsList, int valuesCount) {
        assertTestObserverStateAndValues(testObserver, false, executionId, reportsList, valuesCount);
    }

    private void assertTestObserverStateAndValues(TestObserver<ScenarioExecutionReport> testObserver, boolean testObserverTerminated, Long executionId, List<StepExecutionReportCore> reportsList, int valuesCount) {
        assertTestObserverStateWithValues(testObserver, valuesCount, testObserverTerminated);
        assertScenarioExecutionReport(testObserver.values().get(valuesCount - 1), executionId, reportsList.get(valuesCount - 1));
    }

    private void assertScenarioExecutionReport(ScenarioExecutionReport actual, Long executionId, StepExecutionReportCore rootStepExecutionReportCore) {
        assertThat(actual.scenarioName).isEqualTo(EMPTY_TESTCASE_NAME);
        assertThat(actual.executionId).isEqualTo(executionId);
        assertThat(actual.report.name).isEqualTo(rootStepExecutionReportCore.name);
        assertThat(actual.report.status).isEqualTo(rootStepExecutionReportCore.status);
        assertThat(actual.report.steps)
            .usingRecursiveFieldByFieldElementComparatorOnFields("executionId", "name", "status")
            .containsExactlyElementsOf(rootStepExecutionReportCore.steps);
    }

    private void assertTestObserverStateWithValues(TestObserver<ScenarioExecutionReport> testObserver, int valuesCount, boolean terminated) {
        testObserver
            .assertNoErrors()
            .assertValueCount(valuesCount);
    }

    private static final String EMPTY_TESTCASE_NAME = "empty test case";

    private TestCase emptyTestCase() {
        TestCase mockedTestCase = mock(TestCase.class);
        when(mockedTestCase.id()).thenReturn("1");
        TestCaseMetadataImpl metadata = TestCaseMetadataImpl.builder().withTitle(EMPTY_TESTCASE_NAME).build();
        when(mockedTestCase.metadata()).thenReturn(metadata);
        return mockedTestCase;
    }

    private StepExecutionReportCore stepExecution(String stepName, ServerReportStatus stepStatus, List<StepExecutionReportCore> subStepsReports, Instant startDate) {
        return new StepExecutionReportCoreBuilder()
            .setName(stepName)
            .setStartDate(startDate)
            .setStatus(stepStatus)
            .setSteps(subStepsReports)
            .setInformation(Arrays.asList("info", null, ""))
            .setErrors(Arrays.asList("", "err", null))
            .createStepExecutionReport();
    }

    private Triple<Pair<Observable<StepExecutionReportCore>, Long>, List<StepExecutionReportCore>, TestScheduler> stubEngineExecution(long delay) {
        final List<String> stepNames = Arrays.asList("name", "sub 1", "sub 2");
        Instant startDate = Instant.now();
        final List<StepExecutionReportCore> reportsList = Arrays.asList(
            stepExecution(stepNames.get(0), ServerReportStatus.NOT_EXECUTED,
                Arrays.asList(
                    stepExecution(stepNames.get(1), ServerReportStatus.NOT_EXECUTED, null, startDate),
                    stepExecution(stepNames.get(2), ServerReportStatus.NOT_EXECUTED, null, startDate)), startDate),
            stepExecution(stepNames.get(0), ServerReportStatus.RUNNING,
                Arrays.asList(
                    stepExecution(stepNames.get(1), ServerReportStatus.RUNNING, null, startDate),
                    stepExecution(stepNames.get(2), ServerReportStatus.NOT_EXECUTED, null, startDate)), startDate),
            stepExecution(stepNames.get(0), ServerReportStatus.RUNNING,
                Arrays.asList(
                    stepExecution(stepNames.get(1), ServerReportStatus.SUCCESS, null, startDate),
                    stepExecution(stepNames.get(2), ServerReportStatus.RUNNING, null, startDate)), startDate),
            stepExecution(stepNames.get(0), ServerReportStatus.SUCCESS,
                Arrays.asList(
                    stepExecution(stepNames.get(1), ServerReportStatus.SUCCESS, null, startDate),
                    stepExecution(stepNames.get(2), ServerReportStatus.SUCCESS, null, startDate)), startDate));

        Observable<StepExecutionReportCore> observable = Observable.fromIterable(reportsList);
        TestScheduler testScheduler = null;
        if (delay > 0) {
            testScheduler = new TestScheduler();
            TestScheduler finalTestScheduler = testScheduler;
            observable = observable.concatMap(stepExecutionReportCore -> Observable.just(stepExecutionReportCore)
                .delay(delay, TimeUnit.MILLISECONDS, finalTestScheduler));
        }

        when(executionEngine.executeAndFollow(any())).thenReturn(Pair.of(observable, 0L));

        return Triple.of(Pair.of(observable, 0L), reportsList, testScheduler);
    }

    private ExecutionHistory.Execution stubHistoryExecution(String scenarioId, long executionId) {
        final ImmutableExecutionHistory.Execution storedExecution = ImmutableExecutionHistory.Execution.builder()
            .time(LocalDateTime.now())
            .status(ServerReportStatus.SUCCESS)
            .executionId(executionId)
            .duration(666L)
            .report("")
            .testCaseTitle("fake")
            .environment("")
            .user("")
            .build();

        when(executionHistoryRepository.store(eq(scenarioId), any())).thenReturn(storedExecution);
        when(executionHistoryRepository.getExecution(scenarioId, executionId)).thenReturn(storedExecution);

        return storedExecution;
    }

}
