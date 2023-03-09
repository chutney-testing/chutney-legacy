package com.chutneytesting.engine.domain.execution.command;

import com.chutneytesting.engine.domain.execution.event.Event;

public class PauseExecutionCommand implements Event {

    private final Long executionId;

    public PauseExecutionCommand(Long executionId) {
        this.executionId = executionId;
    }

    @Override
    public long executionId() {
        return executionId;
    }
}
