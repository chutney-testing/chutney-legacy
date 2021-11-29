package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.logTimerState;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.toOutputs;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.valueOf;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MicrometerTimerStopTask implements Task {

    protected static final String OUTPUT_TIMER_SAMPLE_DURATION = "micrometerTimerSampleDuration";

    private final Logger logger;
    private final Timer.Sample sample;
    private final Timer timer;
    private final String timeunit;

    public MicrometerTimerStopTask(Logger logger,
                                   @Input("sample") Timer.Sample sample,
                                   @Input("timer") Timer timer,
                                   @Input("timeunit") String timeunit) {
        this.logger = logger;
        this.sample = sample;
        this.timer = timer;
        this.timeunit = ofNullable(timeunit).orElse(TimeUnit.SECONDS.name());
    }

    @Override
    public List<String> validateInputs() {
        Validator<Timer.Sample> sampleValidation = of(sample)
            .validate(Objects::nonNull, "No sample provided");
        Validator<Timer> timerValidation = of(timer)
            .validate(Objects::nonNull, "No timer provided");
        Validator<String> timeunitValidation = of(timeunit)
            .validate(t -> valueOf(timeunit), noException -> true, "Timeunit not parsable");
        return getErrorsFrom(sampleValidation, timerValidation, timeunitValidation);
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            long duration = sample.stop(timer);
            Duration durationObj = Duration.of(duration, ChronoUnit.NANOS);
            logger.info("Timer sample stopped and last for " + durationObj);
            logTimerState(logger, timer, valueOf(timeunit));
            return TaskExecutionResult.ok(toOutputs(OUTPUT_TIMER_SAMPLE_DURATION, durationObj));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }
}
