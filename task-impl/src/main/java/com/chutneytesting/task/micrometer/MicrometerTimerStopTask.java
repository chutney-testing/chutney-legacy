package com.chutneytesting.task.micrometer;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class MicrometerTimerStopTask implements Task {

    protected static final String OUTPUT_TIMER_SAMPLE_DURATION = "micrometerTimerSampleDuration";

    private final Logger logger;
    private final Timer.Sample sample;
    private final Timer timer;

    public MicrometerTimerStopTask(Logger logger,
                                   @Input("sample") Timer.Sample sample,
                                   @Input("timer") Timer timer) {
        this.logger = logger;
        this.sample = requireNonNull(sample);
        this.timer = requireNonNull(timer);
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            long duration = sample.stop(timer);
            Duration durationObj = Duration.of(duration, ChronoUnit.NANOS);
            logger.info("Timer sample stopped and last for " + durationObj);
            return TaskExecutionResult.ok(toOutputs(durationObj));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private Map<String, Object> toOutputs(Duration duration) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(OUTPUT_TIMER_SAMPLE_DURATION, duration);
        return outputs;
    }
}
