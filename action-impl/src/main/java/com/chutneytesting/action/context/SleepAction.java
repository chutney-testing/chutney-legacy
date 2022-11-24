package com.chutneytesting.action.context;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.durationValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SleepAction implements Action {

    private final Logger logger;
    private final String duration;

    public SleepAction(Logger logger, @Input("duration") String duration) {
        this.logger = logger;
        this.duration = duration;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(durationValidation(duration, "duration"));
    }

    @Override
    public ActionExecutionResult execute() {
        logger.info("Start sleeping for " + duration);
        try {
            TimeUnit.MILLISECONDS.sleep(Duration.parse(duration).toMilliseconds());
        } catch (InterruptedException e) {
            logger.error("Stop sleeping due to Interruption signal");
            return ActionExecutionResult.ko();
        }
        logger.info("Stop sleeping for " + duration);
        return ActionExecutionResult.ok();
    }


}
