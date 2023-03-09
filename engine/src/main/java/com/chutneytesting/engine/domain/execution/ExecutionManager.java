package com.chutneytesting.engine.domain.execution;

import com.chutneytesting.engine.domain.execution.command.PauseExecutionCommand;
import com.chutneytesting.engine.domain.execution.command.ResumeExecutionCommand;
import com.chutneytesting.engine.domain.execution.command.StopExecutionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionManager.class);

    public ExecutionManager() {
    }

    public void pauseExecution(Long executionId) {
        LOGGER.info("Pause requested for " + executionId);
        RxBus.getInstance().post(new PauseExecutionCommand(executionId));
    }

    public void resumeExecution(Long executionId) {
        LOGGER.info("Resume requested for " + executionId);
        RxBus.getInstance().post(new ResumeExecutionCommand(executionId));
    }

    public void stopExecution(Long executionId) {
        LOGGER.info("Stop requested for " + executionId);
        RxBus.getInstance().post(new StopExecutionCommand(executionId));
    }
}
