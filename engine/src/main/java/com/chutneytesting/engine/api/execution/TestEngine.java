package com.chutneytesting.engine.api.execution;

import io.reactivex.Observable;

public interface TestEngine {

    StepExecutionReportDto execute(ExecutionRequestDto request);

    Long executeAsync(ExecutionRequestDto request);

    Observable<StepExecutionReportDto> receiveNotification(Long executionId);

    void pauseExecution(Long executionId);

    void resumeExecution(Long executionId);

    void stopExecution(Long executionId);
}
