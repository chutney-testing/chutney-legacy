package com.chutneytesting.action.micrometer;

import static com.chutneytesting.action.micrometer.MicrometerActionHelper.doubleStringValidation;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.durationStringValidation;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.integerStringValidation;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.parseDoubleOrNull;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.parseDurationOrNull;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.parseIntOrNull;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.parseMapOrNull;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.percentilesListValidation;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.slaListToDoublesValidation;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.toOutputs;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;

public class MicrometerSummaryAction implements Action {

    protected static final String OUTPUT_SUMMARY = "micrometerSummary";

    private final Logger logger;
    private final String name;
    private final String description;
    private final String unit;
    private final List<String> tags;
    private final String bufferLength;
    private final String expiry;
    private final String maxValue;
    private final String minValue;
    private final String percentilePrecision;
    private final Boolean publishPercentilesHistogram;
    private final String percentiles;
    private final String scale;
    private final String sla;

    private DistributionSummary distributionSummary;
    private final MeterRegistry registry;
    private final String record;

    public MicrometerSummaryAction(Logger logger,
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
        this.publishPercentilesHistogram = publishPercentilesHistogram;
        this.distributionSummary = distributionSummary;
        this.registry = ofNullable(registry).orElse(globalRegistry);

        this.bufferLength = bufferLength;
        this.percentilePrecision = percentilePrecision;
        this.expiry = expiry;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.scale = scale;
        this.record = record;
        this.percentiles = percentiles;
        this.sla = sla;
    }

    @Override
    public List<String> validateInputs() {
        Validator<Object> metricNameValidation = of(null)
            .validate(a -> name != null || distributionSummary != null, "name and distributionSummary cannot be both null");

        return getErrorsFrom(
            metricNameValidation,
            integerStringValidation(bufferLength, "bufferLength"),
            integerStringValidation(percentilePrecision, "percentilePrecision"),
            doubleStringValidation(maxValue, "maxValue"),
            doubleStringValidation(minValue, "minValue"),
            doubleStringValidation(scale, "scale"),
            doubleStringValidation(record, "record"),
            durationStringValidation(expiry, "expiry"),
            percentilesListValidation(percentiles),
            slaListToDoublesValidation(sla)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            this.distributionSummary = ofNullable(distributionSummary).orElseGet(() -> this.retrieveSummary(registry));
            if (record != null) {
                distributionSummary.record(parseDoubleOrNull(record));
                logger.info("Distribution summary updated by " + record);
            }
            logger.info("Distribution summary current total is " + distributionSummary.totalAmount());
            logger.info("Distribution summary current max is " + distributionSummary.max());
            logger.info("Distribution summary current mean is " + distributionSummary.mean());
            logger.info("Distribution summary current count is " + distributionSummary.count());
            return ActionExecutionResult.ok(toOutputs(OUTPUT_SUMMARY, distributionSummary));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }

    private DistributionSummary retrieveSummary(MeterRegistry registry) {
        DistributionSummary.Builder builder = DistributionSummary.builder(requireNonNull(name))
            .description(description)
            .baseUnit(unit)
            .distributionStatisticBufferLength(parseIntOrNull(bufferLength))
            .distributionStatisticExpiry(parseDurationOrNull(expiry))
            .maximumExpectedValue(parseDoubleOrNull(maxValue))
            .minimumExpectedValue(parseDoubleOrNull(minValue))
            .percentilePrecision(parseIntOrNull(percentilePrecision))
            .publishPercentileHistogram(publishPercentilesHistogram)
            .publishPercentiles(parseMapOrNull(percentiles, MicrometerActionHelper::parsePercentilesList))
            .serviceLevelObjectives(parseMapOrNull(sla, MicrometerActionHelper::parseSlaListToDoubles));

        ofNullable(scale).ifPresent(t -> builder.scale(parseDoubleOrNull(scale)));
        ofNullable(tags).ifPresent(t -> builder.tags(t.toArray(new String[0])));

        return builder.register(registry);
    }
}
