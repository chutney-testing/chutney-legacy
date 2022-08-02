package com.chutneytesting.execution;

import com.chutneytesting.execution.report.StepExecutionReportCore;
import io.reactivex.Observable;
import org.apache.commons.lang3.tuple.Pair;

public interface ServerTestEngine {

    StepExecutionReportCore execute(ExecutionRequest executionRequest);

    Pair<Observable<StepExecutionReportCore>, Long> executeAndFollow(ExecutionRequest executionRequest);

    void stop(Long executionHash);

    void pause(Long executionHash);

    void resume(Long executionHash);
}
