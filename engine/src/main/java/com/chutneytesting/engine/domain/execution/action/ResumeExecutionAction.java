package com.chutneytesting.engine.domain.execution.action;

import com.chutneytesting.engine.domain.execution.event.Event;

public class ResumeExecutionAction implements Event {

    private Long executionId;

    public ResumeExecutionAction(Long executionId) {
        this.executionId = executionId;
    }

    @Override
    public long executionId() {
        return executionId;
    }
}
