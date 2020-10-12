package com.chutneytesting.task.micrometer;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicrometerSummaryTask implements Task {

    protected static final String OUTPUT_SUMMARY = "micrometerSummary";

    private final Logger logger;
    private final String name;
    private final String description;
    private String unit;
    private final List<String> tags;
    private final Integer bufferLength;
    private final Duration expiry;
    private final Long maxValue;
    private final Long minValue;
    private final Integer percentilePrecision;
    private final Boolean publishPercentilesHistogram;
    private final double[] percentiles;
    private final Double scale;
    private final long[] sla;

    private DistributionSummary distributionSummary;
    private final MeterRegistry registry;
    private final Double record;

    public MicrometerSummaryTask(Logger logger,
                                 @Input("name") String name,
                                 @Input("description") String description,
                                 @Input("unit") String unit,
                                 @Input("tags") List<String> tags,
                                 @Input("bufferLength") String bufferLength,
                                 @Input("expiry") String expiry,
                                 @Input("maxValue") String maxValue,
                                 @Input("minValue") String minValue,
                                 @Input("percentilePrecision") String percentilePrecision,
                                 @Input("publishPercentilesHistogram") Boolean publishPercentilesHistogram,
                                 @Input("percentiles") String percentiles,
                                 @Input("scale") String scale,
                                 @Input("bufferLength") String sla,
                                 @Input("distributionSummary") DistributionSummary distributionSummary,
                                 @Input("registry") MeterRegistry registry,
                                 @Input("record") String record) {
        this.logger = logger;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.tags = tags;
        this.bufferLength = ofNullable(bufferLength).map(Integer::parseInt).orElse(null);
        this.expiry = ofNullable(expiry).map(this::parseDuration).orElse(null);
        this.maxValue = ofNullable(maxValue).map(Long::parseLong).orElse(null);
        this.minValue = ofNullable(minValue).map(Long::parseLong).orElse(null);
        this.percentilePrecision = ofNullable(percentilePrecision).map(Integer::parseInt).orElse(null);
        this.publishPercentilesHistogram = publishPercentilesHistogram;
        this.percentiles = ofNullable(percentiles).map(this::parsePercentilesList).orElse(null);
        this.scale = ofNullable(scale).map(Double::parseDouble).orElse(null);
        this.sla = ofNullable(sla).map(this::parseSlaList).orElse(null);

        this.record = ofNullable(record).map(Double::parseDouble).orElse(null);
        this.distributionSummary = distributionSummary;
        this.registry = registry;
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            this.distributionSummary = ofNullable(distributionSummary).orElseGet(() -> this.retrieveSummary(registry));
            if (record != null) {
                distributionSummary.record(record);
                logger.info("Distribution summary updated by " + record);
            }
            logger.info("Distribution summary current total is " + distributionSummary.totalAmount());
            logger.info("Distribution summary current max is " + distributionSummary.max());
            logger.info("Distribution summary current mean is " + distributionSummary.mean());
            logger.info("Distribution summary current count is " + distributionSummary.count());
            return TaskExecutionResult.ok(toOutputs());
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private DistributionSummary retrieveSummary(MeterRegistry registry) {
        MeterRegistry registryToUse = ofNullable(registry).orElse(globalRegistry);

        DistributionSummary.Builder builder = DistributionSummary.builder(requireNonNull(name))
            .description(description)
            .baseUnit(unit)
            .distributionStatisticBufferLength(bufferLength)
            .distributionStatisticExpiry(expiry)
            .maximumExpectedValue(maxValue)
            .minimumExpectedValue(minValue)
            .percentilePrecision(percentilePrecision)
            .publishPercentileHistogram(publishPercentilesHistogram)
            .publishPercentiles(percentiles)
            .sla(sla);

        ofNullable(scale).ifPresent(t -> builder.scale(scale));
        ofNullable(tags).ifPresent(t -> builder.tags(t.toArray(new String[0])));

        return builder.register(registryToUse);
    }

    private Map<String, Object> toOutputs() {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(OUTPUT_SUMMARY, distributionSummary);
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

    private long[] parseSlaList(String sla) {
        return Arrays.stream(sla.split(","))
            .map(String::trim)
            .mapToLong(Long::parseLong)
            .toArray();
    }
}
