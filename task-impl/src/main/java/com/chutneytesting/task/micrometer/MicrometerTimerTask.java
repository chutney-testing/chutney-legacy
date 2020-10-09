package com.chutneytesting.task.micrometer;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MicrometerTimerTask implements Task {

    protected static final String OUTPUT_TIMER = "micrometerTimer";

    private final Logger logger;
    private final String name;
    private final String description;
    private final List<String> tags;
    private final Integer bufferLength;
    private final Duration expiry;
    private final Duration maxValue;
    private final Duration minValue;
    private final Integer percentilePrecision;
    private final Boolean publishPercentilesHistogram;
    private final double[] percentiles;
    private final Duration[] sla;

    private Timer timer;
    private final MeterRegistry registry;
    private final TimeUnit timeunit;
    private final Duration record;

    public MicrometerTimerTask(Logger logger,
                               @Input("name") String name,
                               @Input("description") String description,
                               @Input("tags") List<String> tags,
                               @Input("bufferLength") String bufferLength,
                               @Input("expiry") String expiry,
                               @Input("maxValue") String maxValue,
                               @Input("minValue") String minValue,
                               @Input("percentilePrecision") String percentilePrecision,
                               @Input("publishPercentilesHistogram") Boolean publishPercentilesHistogram,
                               @Input("percentiles") String percentiles,
                               @Input("bufferLength") String sla,
                               @Input("timer") Timer timer,
                               @Input("registry") MeterRegistry registry,
                               @Input("timeunit") String timeunit,
                               @Input("record") String record) {
        this.logger = logger;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.bufferLength = ofNullable(bufferLength).map(Integer::parseInt).orElse(null);
        this.expiry = ofNullable(expiry).map(this::parseDuration).orElse(null);
        this.maxValue = ofNullable(maxValue).map(this::parseDuration).orElse(null);
        this.minValue = ofNullable(minValue).map(this::parseDuration).orElse(null);
        this.percentilePrecision = ofNullable(percentilePrecision).map(Integer::parseInt).orElse(null);
        this.publishPercentilesHistogram = publishPercentilesHistogram;
        this.percentiles = ofNullable(percentiles).map(this::parsePercentilesList).orElse(null);
        this.sla = ofNullable(sla).map(this::parseSlaList).orElse(null);

        this.timeunit = ofNullable(timeunit).map(TimeUnit::valueOf).orElse(TimeUnit.SECONDS);
        this.record = ofNullable(record).map(this::parseDuration).orElse(null);
        this.timer = timer;
        this.registry = registry;
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            this.timer = ofNullable(timer).orElseGet(() -> this.retrieveTimer(registry));
            if (record != null) {
                timer.record(record);
                logger.info("Timer updated by " + record);
            }
            logger.info("Timer current total time is " + timer.totalTime(timeunit) + " " + timeunit);
            logger.info("Timer current max time is " + timer.max(timeunit) + " " + timeunit);
            logger.info("Timer current mean time is " + timer.mean(timeunit) + " " + timeunit);
            logger.info("Timer current count is " + timer.count());
            return TaskExecutionResult.ok(toOutputs());
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private Timer retrieveTimer(MeterRegistry registry) {
        MeterRegistry registryToUse = ofNullable(registry).orElse(globalRegistry);

        Timer.Builder builder = Timer.builder(requireNonNull(name))
            .description(description)
            .distributionStatisticBufferLength(bufferLength)
            .distributionStatisticExpiry(expiry)
            .maximumExpectedValue(maxValue)
            .minimumExpectedValue(minValue)
            .percentilePrecision(percentilePrecision)
            .publishPercentileHistogram(publishPercentilesHistogram)
            .publishPercentiles(percentiles)
            .sla(sla);

        ofNullable(tags).ifPresent(t -> builder.tags(t.toArray(new String[0])));

        return builder.register(registryToUse);
    }

    private Map<String, Object> toOutputs() {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(OUTPUT_TIMER, timer);
        return outputs;
    }

    private Duration parseDuration(String duration) {
        return Duration.of(com.chutneytesting.task.spi.time.Duration.parse(duration).toMilliseconds(), ChronoUnit.MILLIS);
    }

    private double[] parsePercentilesList(String percentiles) {
        return Arrays.stream(percentiles.split(","))
            .map(String::trim)
            .mapToDouble(Double::parseDouble)
            .toArray();
    }

    private Duration[] parseSlaList(String sla) {
        return Arrays.stream(sla.split(","))
            .map(String::trim)
            .map(this::parseDuration)
            .toArray(Duration[]::new);
    }
}
