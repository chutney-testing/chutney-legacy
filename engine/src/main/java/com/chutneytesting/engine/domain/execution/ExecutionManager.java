package com.chutneytesting.engine.domain.execution;

import com.chutneytesting.engine.domain.execution.action.PauseExecutionAction;
import com.chutneytesting.engine.domain.execution.action.ResumeExecutionAction;
import com.chutneytesting.engine.domain.execution.action.StopExecutionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionManager.class);

    public ExecutionManager() {
    }

    public void pauseExecution(Long executionId) {
        LOGGER.info("Pause requested for " + executionId);
        RxBus.getInstance().post(new PauseExecutionAction(executionId));
    }

    public void resumeExecution(Long executionId) {
        LOGGER.info("Resume requested for " + executionId);
        RxBus.getInstance().post(new ResumeExecutionAction(executionId));
    }

    public void stopExecution(Long executionId) {
        LOGGER.info("Stop requested for " + executionId);
        RxBus.getInstance().post(new StopExecutionAction(executionId));
    }
}
