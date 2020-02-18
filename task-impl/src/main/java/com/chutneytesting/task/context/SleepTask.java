package com.chutneytesting.task.context;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.time.Duration;
import java.util.concurrent.TimeUnit;

public class SleepTask implements Task {

    private final Logger logger;
    private final Duration duration;

    public SleepTask(Logger logger, @Input("duration") String duration) {
        this.logger = logger;
        this.duration = Duration.parse(duration);
    }

    @Override
    public TaskExecutionResult execute() {
        logger.info("Start sleeping for " + duration);
        try {
            TimeUnit.MILLISECONDS.sleep(duration.toMilliseconds());
        } catch (InterruptedException e) {
            logger.error("Stop sleeping due to Interruption signal");
            return TaskExecutionResult.ko();
        }
        logger.info("Stop sleeping for " + duration);
        return TaskExecutionResult.ok();
    }


}
