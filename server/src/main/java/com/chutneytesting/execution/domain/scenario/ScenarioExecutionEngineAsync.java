package com.chutneytesting.execution.domain.scenario;

import static io.reactivex.schedulers.Schedulers.io;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.compiler.TestCasePreProcessors;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ExecutionHistory.DetachedExecution;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
import com.chutneytesting.execution.domain.report.ScenarioExecutionReport;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.execution.domain.report.StepExecutionReportCore;
import com.chutneytesting.execution.domain.state.ExecutionStateRepository;
import com.chutneytesting.instrument.domain.Metrics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Ascii;
import com.google.common.base.Joiner;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScenarioExecutionEngineAsync {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioExecutionEngineAsync.class);
    private static final long DEFAULT_RETENTION_DELAY_SECONDS = 5;
    private static final long DEFAULT_DEBOUNCE_MILLISECONDS = 100;

    private final ObjectMapper objectMapper;

    private final ExecutionHistoryRepository executionHistoryRepository;
    private final ServerTestEngine executionEngine;
    private final ExecutionStateRepository executionStateRepository;
    private final Metrics metrics;
    private final TestCasePreProcessors testCasePreProcessors;
    private final DataSetHistoryRepository dataSetHistoryRepository;

    private Map<Long, Pair<Observable<ScenarioExecutionReport>, Long>> scenarioExecutions = new ConcurrentHashMap<>();
    private long retentionDelaySeconds;
    private long debounceMilliSeconds;

    public ScenarioExecutionEngineAsync(ExecutionHistoryRepository executionHistoryRepository,
                                        ServerTestEngine executionEngine,
                                        ExecutionStateRepository executionStateRepository,
                                        Metrics metrics,
                                        TestCasePreProcessors testCasePreProcessors,
                                        ObjectMapper objectMapper,
                                        DataSetHistoryRepository dataSetHistoryRepository) {
        this(executionHistoryRepository, executionEngine, executionStateRepository, metrics, testCasePreProcessors, objectMapper, dataSetHistoryRepository, DEFAULT_RETENTION_DELAY_SECONDS, DEFAULT_DEBOUNCE_MILLISECONDS);
    }

    public ScenarioExecutionEngineAsync(ExecutionHistoryRepository executionHistoryRepository,
                                        ServerTestEngine executionEngine,
                                        ExecutionStateRepository executionStateRepository,
                                        Metrics metrics,
                                        TestCasePreProcessors testCasePreProcessors,
                                        ObjectMapper objectMapper,
                                        DataSetHistoryRepository dataSetHistoryRepository,
                                        long retentionDelaySeconds,
                                        long debounceMilliSeconds) {
        this.executionHistoryRepository = executionHistoryRepository;
        this.executionEngine = executionEngine;
        this.executionStateRepository = executionStateRepository;
        this.metrics = metrics;
        this.testCasePreProcessors = testCasePreProcessors;
        this.objectMapper = objectMapper;
        this.dataSetHistoryRepository = dataSetHistoryRepository;
        this.retentionDelaySeconds = retentionDelaySeconds;
        this.debounceMilliSeconds = debounceMilliSeconds;
    }

    /**
     * Execute a test case with ExecutionEngine and store StepExecutionReport.
     *
     * @param executionRequest with the test case to execute and the environment chosen
     * @return execution id.
     */
    public Long execute(ExecutionRequest executionRequest) {
        // Compile testcase for execution
        ExecutionRequest executionRequestProcessed = new ExecutionRequest(testCasePreProcessors.apply(executionRequest), executionRequest.environment, executionRequest.withScenarioDefaultDataSet);
        // Initialize execution history
        ExecutionHistory.Execution storedExecution = storeInitialReport(executionRequestProcessed);
        // Start engine execution
        Pair<Observable<StepExecutionReportCore>, Long> followResult = callEngineExecution(executionRequestProcessed, storedExecution);
        // Build execution observable
        Observable<ScenarioExecutionReport> executionObservable = buildScenarioExecutionReportObservable(executionRequestProcessed, storedExecution.executionId(), followResult);
        // Store execution Observable to permit further subscriptions
        LOGGER.trace("Add replayer for execution {}", storedExecution.executionId());
        scenarioExecutions.put(storedExecution.executionId(), Pair.of(executionObservable, followResult.getRight()));
        LOGGER.debug("Replayers map size : {}", scenarioExecutions.size());
        // Begin execution
        executionObservable.subscribeOn(io()).subscribe();
        // Return execution id
        return storedExecution.executionId();
    }

    private Pair<Observable<StepExecutionReportCore>, Long> callEngineExecution(ExecutionRequest executionRequest, ExecutionHistory.Execution storedExecution) {
        Pair<Observable<StepExecutionReportCore>, Long> followResult;
        try {
            followResult = executionEngine.executeAndFollow(executionRequest);
        } catch (Exception e) {
            LOGGER.error("Cannot execute test case [" + executionRequest.testCase.id() + "]", e.getMessage());
            setExecutionToFailed(executionRequest.testCase.id(), storedExecution, ofNullable(e.getMessage()).orElse(e.toString()));
            throw new FailedExecutionAttempt(e, storedExecution.executionId(), executionRequest.testCase.metadata().title());
        }
        return followResult;
    }

    Observable<ScenarioExecutionReport> buildScenarioExecutionReportObservable(ExecutionRequest executionRequest, Long executionId, Pair<Observable<StepExecutionReportCore>, Long> engineExecution) {
        // Observe in background
        Observable<StepExecutionReportCore> replayer = engineExecution.getLeft().observeOn(io());
        // Debounce configuration
        if (debounceMilliSeconds > 0) {
            replayer = replayer.debounce(debounceMilliSeconds, TimeUnit.MILLISECONDS);
        }
        return replayer
            .doOnSubscribe(disposable -> notifyExecutionStart(executionId, executionRequest.testCase))

            // Create report
            .map(report -> {
                LOGGER.trace("Map report for execution {}", executionId);
                return new ScenarioExecutionReport(executionId, executionRequest.testCase.metadata().title(), executionRequest.environment, report);
            })

            .doOnNext(report -> updateHistory(executionId, executionRequest, report))

            .doOnTerminate(() -> notifyExecutionEnd(executionId, executionRequest.testCase))
            .doOnTerminate(() -> sendMetrics(executionId, executionRequest.testCase))
            .doOnTerminate(() -> cleanExecutionId(executionId))

            // Make hot with replay last state
            .replay(1)
            // Begin process on the first subscribe
            .autoConnect();
    }

    private void setExecutionToFailed(String scenarioId, ExecutionHistory.Execution storedExecution, String errorMessage) {
        ImmutableExecutionHistory.Execution execution = ImmutableExecutionHistory.Execution.copyOf(storedExecution)
            .withStatus(ServerReportStatus.FAILURE)
            .withError(errorMessage);
        executionHistoryRepository.update(scenarioId, execution);
    }

    private ExecutionHistory.Execution storeInitialReport(ExecutionRequest executionRequest) {
        Optional<Pair<String, Integer>> executionDataSet = findExecutionDataSet(executionRequest);
        DetachedExecution detachedExecution = ImmutableExecutionHistory.DetachedExecution.builder()
            .time(LocalDateTime.now())
            .duration(0L)
            .status(ServerReportStatus.RUNNING)
            .info("")
            .error("")
            .report("")
            .testCaseTitle(executionRequest.testCase.metadata().title())
            .environment(executionRequest.environment)
            .datasetId(executionDataSet.map(Pair::getLeft))
            .datasetVersion(executionDataSet.map(Pair::getRight))
            .build();

        return executionHistoryRepository.store(executionRequest.testCase.id(), detachedExecution);
    }

    public Observable<ScenarioExecutionReport> followExecution(String scenarioId, Long executionId) {
        if (scenarioExecutions.containsKey(executionId)) {
            return scenarioExecutions.get(executionId).getLeft();
        } else {
            throw new ScenarioNotRunningException(scenarioId);
        }
    }

    public void stop(String scenarioId, Long executionId) {
        if (scenarioExecutions.containsKey(executionId)) {
            executionEngine.stop(scenarioExecutions.get(executionId).getRight());
        } else {
            throw new ScenarioNotRunningException(scenarioId);
        }
    }

    public void pause(String scenarioId, Long executionId) {
        if (scenarioExecutions.containsKey(executionId)) {
            executionEngine.pause(scenarioExecutions.get(executionId).getRight());
        } else {
            throw new ScenarioNotRunningException(scenarioId);
        }
    }

    public void resume(String scenarioId, Long executionId) {
        if (scenarioExecutions.containsKey(executionId)) {
            executionEngine.resume(scenarioExecutions.get(executionId).getRight());
        } else {
            throw new ScenarioNotRunningException(scenarioId);
        }
    }

    public void setRetentionDelaySeconds(long retentionDelaySeconds) {
        this.retentionDelaySeconds = retentionDelaySeconds;
    }

    public void setDebounceMilliSeconds(long debounceMilliSeconds) {
        this.debounceMilliSeconds = debounceMilliSeconds;
    }

    /**
     * Build a {@link DetachedExecution} to store via {@link ExecutionHistoryRepository}
     *
     * @param scenarioReport report to summarize
     */
    private DetachedExecution summarize(ScenarioExecutionReport scenarioReport, String environment) {
        return ImmutableExecutionHistory.DetachedExecution.builder()
            .time(scenarioReport.report.startDate.atZone(ZoneId.systemDefault()).toLocalDateTime())
            .duration(scenarioReport.report.duration)
            .status(scenarioReport.report.status)
            .info(joinAndTruncateMessages(searchInfo(scenarioReport.report)))
            .error(joinAndTruncateMessages(searchErrors(scenarioReport.report)))
            .report(serialize(scenarioReport)) // TODO - type me and move serialization to infra
            .testCaseTitle(scenarioReport.scenarioName)
            .environment(environment)
            .build();
    }

    private String serialize(ScenarioExecutionReport stepExecutionReport) {
        try {
            return objectMapper.writeValueAsString(stepExecutionReport);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to serialize StepExecutionReport content with name='{}'", stepExecutionReport.report.name, e);
            return "{}";
        }
    }

    private Optional<String> joinAndTruncateMessages(Iterable<String> messages) {
        return Optional.of(Ascii.truncate(Joiner.on(", ").join(messages), 50, "...")).filter(s -> !s.isEmpty());
    }

    private void notifyExecutionStart(long executionId, TestCase testCase) {
        LOGGER.trace("Notify start for execution {}", executionId);
        executionStateRepository.notifyExecutionStart(testCase.id());
    }

    private void cleanExecutionId(long executionId) {
        LOGGER.trace("Clean for execution {}", executionId);
        if (retentionDelaySeconds > 0) {
            Completable.timer(retentionDelaySeconds, TimeUnit.SECONDS)
                .subscribe(() -> {
                    LOGGER.trace("Remove replayer for execution {}", executionId);
                    scenarioExecutions.remove(executionId);
                }, throwable -> LOGGER.error("Cannot remove replayer for execution {}", executionId, throwable));
        } else {
            scenarioExecutions.remove(executionId);
        }
    }

    private void sendMetrics(long executionId, TestCase testCase) {
        LOGGER.trace("Send metrics for execution {}", executionId);
        try {
            ExecutionHistory.Execution execution = executionHistoryRepository.getExecution(testCase.id(), executionId);
            metrics.onExecutionEnded(testCase.metadata().title(), execution.status(), execution.duration());
        } catch (Exception e) {
            LOGGER.error("Send metrics for execution {} failed", executionId, e);
        }
    }

    private void updateHistory(long executionId, ExecutionRequest executionRequest, ScenarioExecutionReport report) {
        LOGGER.trace("Update history for execution {}", executionId);
        try {
            executionHistoryRepository.update(executionRequest.testCase.id(), summarize(report, executionRequest.environment).attach(executionId));
        } catch (Exception e) {
            LOGGER.error("Update history for execution {} failed", executionId, e);
        }
    }

    private void notifyExecutionEnd(long executionId, TestCase testCase) {
        LOGGER.trace("Notify end for execution {}", executionId);
        executionStateRepository.notifyExecutionEnd(testCase.id());
    }

    private static List<String> searchInfo(StepExecutionReportCore report) {
        if (report.information.isEmpty()) {
            return report.steps.stream()
                .map(ScenarioExecutionEngineAsync::searchInfo)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        } else {
            return report.information;
        }
    }

    private static List<String> searchErrors(StepExecutionReportCore report) {
        if (report.errors.isEmpty()) {
            return report.steps.stream()
                .map(ScenarioExecutionEngineAsync::searchErrors)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        } else {
            return report.errors;
        }
    }

    private Optional<Pair<String, Integer>> findExecutionDataSet(ExecutionRequest executionRequest) {
        if (executionRequest.withScenarioDefaultDataSet) {
            return executionRequest.testCase.metadata().datasetId()
                .map(datasetId -> Pair.of(datasetId, dataSetHistoryRepository.lastVersion(datasetId)));
        }
        return empty();
    }
}
