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

package com.chutneytesting.execution.infra.execution;

import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.ServerTestEngine;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import io.reactivex.rxjava3.core.Observable;
import org.apache.commons.lang3.tuple.Pair;

public class ServerTestEngineJavaImpl implements ServerTestEngine {

    private final TestEngine executionEngine;
    private final ExecutionRequestMapper executionRequestMapper;

    public ServerTestEngineJavaImpl(TestEngine executionEngine,
                                    ExecutionRequestMapper executionRequestMapper) {
        this.executionEngine = executionEngine;
        this.executionRequestMapper = executionRequestMapper;
    }

    @Override
    public StepExecutionReportCore execute(ExecutionRequest executionRequest) {
        ExecutionRequestDto executionRequestDto = executionRequestMapper.toDto(executionRequest);
        StepExecutionReportDto stepExecutionReportDto = executionEngine.execute(executionRequestDto);
        return StepExecutionReportMapperCore.fromDto(stepExecutionReportDto);
    }

    @Override
    public Pair<Observable<StepExecutionReportCore>, Long> executeAndFollow(ExecutionRequest executionRequest) {
        ExecutionRequestDto executionRequestDto = executionRequestMapper.toDto(executionRequest);
        Long executionId = executionEngine.executeAsync(executionRequestDto);
        return Pair.of(
            executionEngine.receiveNotification(executionId).map(StepExecutionReportMapperCore::fromDto),
            executionId
        );
    }

    @Override
    public void stop(Long executionHash) {
        executionEngine.stopExecution(executionHash);
    }

    @Override
    public void pause(Long executionHash) {
        executionEngine.pauseExecution(executionHash);
    }

    @Override
    public void resume(Long executionHash) {
        executionEngine.resumeExecution(executionHash);
    }
}
