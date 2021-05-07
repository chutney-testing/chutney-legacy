package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.checkDoubleOrNull;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.checkDurationOrNull;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.checkIntOrNull;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.checkMapOrNull;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.checkRegistry;
import static com.chutneytesting.task.micrometer.MicrometerTaskHelper.toOutputs;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.List;

public class MicrometerSummaryTask implements Task {

    protected static final String OUTPUT_SUMMARY = "micrometerSummary";

    private final Logger logger;
    private final String name;
    private final String description;
    private final String unit;
    private final List<String> tags;
    private final Integer bufferLength;
    private final Duration expiry;
    private final Double maxValue;
    private final Double minValue;
    private final Integer percentilePrecision;
    private final Boolean publishPercentilesHistogram;
    private final double[] percentiles;
    private final Double scale;
    private final double[] sla;

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
                                 @Input("sla") String sla,
                                 @Input("distributionSummary") DistributionSummary distributionSummary,
                                 @Input("registry") MeterRegistry registry,
                                 @Input("record") String record) {
        this.logger = logger;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.tags = tags;
        this.bufferLength = checkIntOrNull(bufferLength);
        this.expiry = checkDurationOrNull(expiry);
        this.maxValue = checkDoubleOrNull(maxValue);
        this.minValue = checkDoubleOrNull(minValue);
        this.percentilePrecision = checkIntOrNull(percentilePrecision);
        this.publishPercentilesHistogram = publishPercentilesHistogram;
        this.percentiles = checkMapOrNull(percentiles, MicrometerTaskHelper::parsePercentilesList);
        this.scale = checkDoubleOrNull(scale);
        this.sla = checkMapOrNull(sla, MicrometerTaskHelper::parseSlaListToDoubles);

        this.record = checkDoubleOrNull(record);
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
            return TaskExecutionResult.ok(toOutputs(OUTPUT_SUMMARY, distributionSummary));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private DistributionSummary retrieveSummary(MeterRegistry registry) {
        MeterRegistry registryToUse = checkRegistry(registry);

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
            .serviceLevelObjectives(sla);

        ofNullable(scale).ifPresent(t -> builder.scale(scale));
        ofNullable(tags).ifPresent(t -> builder.tags(t.toArray(new String[0])));

        return builder.register(registryToUse);
    }
}
