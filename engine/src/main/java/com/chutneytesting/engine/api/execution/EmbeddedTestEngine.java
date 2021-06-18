package com.chutneytesting.engine.api.execution;

import com.chutneytesting.engine.domain.execution.ExecutionEngine;
import com.chutneytesting.engine.domain.execution.ExecutionManager;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.report.Reporter;
import com.chutneytesting.task.spi.injectable.TasksConfiguration;
import io.reactivex.Observable;

public final class EmbeddedTestEngine implements TestEngine {

    private final ExecutionEngine engine;
    private final Reporter reporter;
    private final ExecutionManager executionManager;
    private final TasksConfiguration tasksConfiguration;

    public EmbeddedTestEngine(ExecutionEngine engine, Reporter reporter, ExecutionManager executionManager, TasksConfiguration tasksConfiguration) {
        this.engine = engine;
        this.reporter = reporter;
        this.executionManager = executionManager;
        this.tasksConfiguration = tasksConfiguration;
    }

    @Override
    public StepExecutionReportDto execute(ExecutionRequestDto request) {
        Long executionId = executeAsync(request);
        return receiveNotification(executionId).blockingLast();
    }

    @Override
    public Long executeAsync(ExecutionRequestDto request) {
        StepDefinition stepDefinition = StepDefinitionMapper.toStepDefinition(request.scenario.definition);
        return engine.execute(stepDefinition, ScenarioExecution.createScenarioExecution(tasksConfiguration));
    }

    @Override
    public Observable<StepExecutionReportDto> receiveNotification(Long executionId) {
        return reporter.subscribeOnExecution(executionId)
            .map(StepExecutionReportMapper::toDto);
    }

    @Override
    public void pauseExecution(Long executionId) {
        executionManager.pauseExecution(executionId);
    }

    @Override
    public void resumeExecution(Long executionId) {
        executionManager.resumeExecution(executionId);
    }

    @Override
    public void stopExecution(Long executionId) {
        executionManager.stopExecution(executionId);
    }
}
