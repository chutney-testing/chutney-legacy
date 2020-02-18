package com.chutneytesting.engine.domain.execution.action;

import com.chutneytesting.engine.domain.execution.event.Event;

public class PauseExecutionAction implements Event {

    private Long executionId;

    public PauseExecutionAction(Long executionId) {
        this.executionId = executionId;
    }

    @Override
    public long executionId() {
        return executionId;
    }
}
