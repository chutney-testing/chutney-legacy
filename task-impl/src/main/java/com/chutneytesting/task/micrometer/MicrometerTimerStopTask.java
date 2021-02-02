package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.checkTimeUnit;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.logTimerState;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.toOutputs;
import static java.util.Objects.requireNonNull;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class MicrometerTimerStopTask implements Task {

    protected static final String OUTPUT_TIMER_SAMPLE_DURATION = "micrometerTimerSampleDuration";

    private final Logger logger;
    private final Timer.Sample sample;
    private final Timer timer;
    private TimeUnit timeunit;

    public MicrometerTimerStopTask(Logger logger,
                                   @Input("sample") Timer.Sample sample,
                                   @Input("timer") Timer timer,
                                   @Input("timeunit") String timeunit) {
        this.logger = logger;
        this.sample = requireNonNull(sample);
        this.timer = requireNonNull(timer);
        this.timeunit = checkTimeUnit(timeunit);
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            long duration = sample.stop(timer);
            Duration durationObj = Duration.of(duration, ChronoUnit.NANOS);
            logger.info("Timer sample stopped and last for " + durationObj);
            logTimerState(logger, timer, timeunit);
            return TaskExecutionResult.ok(toOutputs(OUTPUT_TIMER_SAMPLE_DURATION, durationObj));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }
}
