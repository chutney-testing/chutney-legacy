package com.chutneytesting.task.amqp;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import org.apache.qpid.server.SystemLauncher;

public class QpidServerStopTask implements Task {

    private final Logger logger;
    private final SystemLauncher systemLauncher;

    public QpidServerStopTask(Logger logger, @Input("qpid-launcher") SystemLauncher systemLauncher) {
        this.logger = logger;
        this.systemLauncher = systemLauncher;
    }

    @Override
    public TaskExecutionResult execute() {
        logger.info("Call Qpid Server shutdown");
        systemLauncher.shutdown();
        return TaskExecutionResult.ok();
    }
}
