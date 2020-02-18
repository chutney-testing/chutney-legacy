package com.chutneytesting.engine.domain.execution.action;

import com.chutneytesting.engine.domain.execution.event.Event;

public class StopExecutionAction implements Event {

    private Long executionId;

    public StopExecutionAction(Long executionId) {
        this.executionId = executionId;
    }

    @Override
    public long executionId() {
        return executionId;
    }
}
