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

package com.chutneytesting.engine.api.execution;

import com.chutneytesting.action.spi.injectable.ActionsConfiguration;
import com.chutneytesting.engine.domain.execution.ExecutionEngine;
import com.chutneytesting.engine.domain.execution.ExecutionManager;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.Dataset;
import com.chutneytesting.engine.domain.execution.engine.Environment;
import com.chutneytesting.engine.domain.report.Reporter;
import io.reactivex.rxjava3.core.Observable;
import java.util.Optional;

public final class EmbeddedTestEngine implements TestEngine {

    private final ExecutionEngine engine;
    private final Reporter reporter;
    private final ExecutionManager executionManager;
    private final ActionsConfiguration actionsConfiguration;

    public EmbeddedTestEngine(ExecutionEngine engine, Reporter reporter, ExecutionManager executionManager, ActionsConfiguration actionsConfiguration) {
        this.engine = engine;
        this.reporter = reporter;
        this.executionManager = executionManager;
        this.actionsConfiguration = actionsConfiguration;
    }

    @Override
    public StepExecutionReportDto execute(ExecutionRequestDto request) {
        Long executionId = executeAsync(request);
        return receiveNotification(executionId).blockingLast();
    }

    @Override
    public Long executeAsync(ExecutionRequestDto request) {
        StepDefinition stepDefinition = StepDefinitionMapper.toStepDefinition(request.scenario.definition);
        Dataset dataset = Optional.ofNullable(request.dataset)
            .map(d -> new Dataset(d.constants, d.datatable))
            .orElseGet(Dataset::new);
        Environment environment = EnvironmentDtoMapper.INSTANCE.toDomain(request.environment);
        return engine.execute(
            stepDefinition,
            dataset,
            ScenarioExecution.createScenarioExecution(actionsConfiguration),
            environment);
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

    @Override
    public void close() {
        engine.shutdown();
    }
}
