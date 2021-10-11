package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.doubleStringValidation;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.parseDoubleOrNull;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.toOutputs;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;

public class MicrometerCounterTask implements Task {

    protected static final String OUTPUT_COUNTER = "micrometerCounter";

    private final Logger logger;
    private final String name;
    private final String description;
    private final String unit;
    private final List<String> tags;
    private Counter counter;
    private final MeterRegistry registry;
    private final String increment;

    public MicrometerCounterTask(Logger logger,
                                 @Input("name") String name,
                                 @Input("description") String description,
                                 @Input("unit") String unit,
                                 @Input("tags") List<String> tags,
                                 @Input("counter") Counter counter,
                                 @Input("registry") MeterRegistry registry,
                                 @Input("increment") String increment) {
        this.logger = logger;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.tags = tags;
        this.increment = increment;
        this.counter = counter;
        this.registry = ofNullable(registry).orElse(globalRegistry);
    }

    @Override
    public List<String> validateInputs() {
        Validator<Object> metricNameValidation = of(null)
            .validate(a -> name != null || counter != null, "name and counter cannot be both null");

        return getErrorsFrom(
            metricNameValidation,
            doubleStringValidation(increment, "increment")
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            this.counter = ofNullable(counter).orElseGet(() -> this.retrieveCounter(registry));
            if (increment != null) {
                counter.increment(parseDoubleOrNull(increment));
                logger.info("Counter incremented by " + increment);
            }
            logger.info("Counter current count is " + counter.count());
            return TaskExecutionResult.ok(toOutputs(OUTPUT_COUNTER, counter));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private Counter retrieveCounter(MeterRegistry registry) {

        Counter.Builder builder = Counter.builder(requireNonNull(name))
            .description(description)
            .baseUnit(unit);

        ofNullable(tags).ifPresent(t -> builder.tags(t.toArray(new String[0])));

        return builder.register(registry);
    }
}
