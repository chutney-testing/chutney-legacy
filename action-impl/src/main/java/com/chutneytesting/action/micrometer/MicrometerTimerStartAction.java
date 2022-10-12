package com.chutneytesting.action.micrometer;

import static com.chutneytesting.action.micrometer.MicrometerActionHelper.toOutputs;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class MicrometerTimerStartAction implements Action {

    protected static final String OUTPUT_TIMER_SAMPLE = "micrometerTimerSample";

    private final Logger logger;
    private final MeterRegistry registry;

    public MicrometerTimerStartAction(Logger logger,
                                    @Input("registry") MeterRegistry registry) {
        this.logger = logger;
        this.registry = ofNullable(registry).orElse(globalRegistry);
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            Timer.Sample sample = Timer.start(registry);
            logger.info("Timing sample started");
            return ActionExecutionResult.ok(toOutputs(OUTPUT_TIMER_SAMPLE, sample));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }
}
