package com.chutneytesting.engine.domain.execution.command;

import com.chutneytesting.engine.domain.execution.event.Event;

public class StopExecutionCommand implements Event {

    private final Long executionId;

    public StopExecutionCommand(Long executionId) {
        this.executionId = executionId;
    }

    @Override
    public long executionId() {
        return executionId;
    }
}
