package com.chutneytesting.action.amqp;

import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import java.util.List;
import java.util.Objects;
import org.apache.qpid.server.SystemLauncher;

public class QpidServerStopAction implements Action {

    private final Logger logger;
    private final SystemLauncher systemLauncher;

    public QpidServerStopAction(Logger logger, @Input("qpid-launcher") SystemLauncher systemLauncher) {
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
    public ActionExecutionResult execute() {
        logger.info("Call Qpid Server shutdown");
        systemLauncher.shutdown();
        return ActionExecutionResult.ok();
    }
}
