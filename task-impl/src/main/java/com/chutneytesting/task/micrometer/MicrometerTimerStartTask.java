package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.checkRegistry;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.toOutputs;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class MicrometerTimerStartTask implements Task {

    protected static final String OUTPUT_TIMER_SAMPLE = "micrometerTimerSample";

    private final Logger logger;
    private MeterRegistry registry;

    public MicrometerTimerStartTask(Logger logger,
                                    @Input("registry") MeterRegistry registry) {
        this.logger = logger;
        this.registry = checkRegistry(registry);
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            Timer.Sample sample = Timer.start(registry);
            logger.info("Timing sample started");
            return TaskExecutionResult.ok(toOutputs(OUTPUT_TIMER_SAMPLE, sample));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }
}
