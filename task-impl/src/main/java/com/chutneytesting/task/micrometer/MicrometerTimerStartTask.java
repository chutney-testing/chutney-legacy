package com.chutneytesting.task.micrometer;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.HashMap;
import java.util.Map;

public class MicrometerTimerStartTask implements Task {

    protected static final String OUTPUT_TIMER_SAMPLE = "micrometerTimerSample";

    private final Logger logger;
    private MeterRegistry registry;

    public MicrometerTimerStartTask(Logger logger,
                                    @Input("registry") MeterRegistry registry) {
        this.logger = logger;
        this.registry = ofNullable(registry).orElse(globalRegistry);
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            Timer.Sample sample = Timer.start(registry);
            logger.info("Timing sample started");
            return TaskExecutionResult.ok(toOutputs(sample));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private Map<String, Object> toOutputs(Timer.Sample sample) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(OUTPUT_TIMER_SAMPLE, sample);
        return outputs;
    }
}
