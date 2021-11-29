package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import java.util.List;
import java.util.Objects;
import org.apache.qpid.server.SystemLauncher;

public class QpidServerStopTask implements Task {

    private final Logger logger;
    private final SystemLauncher systemLauncher;

    public QpidServerStopTask(Logger logger, @Input("qpid-launcher") SystemLauncher systemLauncher) {
        this.logger = logger;
        this.systemLauncher = systemLauncher;
    }

    @Override
    public List<String> validateInputs() {
        Validator<SystemLauncher> systemValidation = of(systemLauncher)
            .validate(Objects::nonNull, "No qpid-launcher provided");
        return getErrorsFrom(systemValidation);
    }

    @Override
    public TaskExecutionResult execute() {
        logger.info("Call Qpid Server shutdown");
        systemLauncher.shutdown();
        return TaskExecutionResult.ok();
    }
}
