package com.chutneytesting.execution.infra.execution;

import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.ExecutionRequestMapper;
import com.chutneytesting.server.core.domain.execution.ServerTestEngine;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import io.reactivex.Observable;
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
