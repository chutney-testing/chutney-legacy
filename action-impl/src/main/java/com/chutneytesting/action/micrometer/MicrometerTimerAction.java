package com.chutneytesting.action.micrometer;

import static com.chutneytesting.action.micrometer.MicrometerActionHelper.durationStringValidation;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.integerStringValidation;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.logTimerState;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.parseDuration;
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
import static java.util.concurrent.TimeUnit.valueOf;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MicrometerTimerAction implements Action {

    protected static final String OUTPUT_TIMER = "micrometerTimer";

    private final Logger logger;
    private final String name;
    private final String description;
    private final List<String> tags;
    private final String bufferLength;
    private final String expiry;
    private final String maxValue;
    private final String minValue;
    private final String percentilePrecision;
    private final Boolean publishPercentilesHistogram;
    private final String percentiles;
    private final String sla;

    private Timer timer;
    private final MeterRegistry registry;
    private final String timeunit;
    private final String record;

    public MicrometerTimerAction(Logger logger,
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
                               @Input("sla") String sla,
                               @Input("timer") Timer timer,
                               @Input("registry") MeterRegistry registry,
                               @Input("timeunit") String timeunit,
                               @Input("record") String record) {
        this.logger = logger;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.bufferLength = bufferLength;
        this.expiry = expiry;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.percentilePrecision = percentilePrecision;
        this.publishPercentilesHistogram = publishPercentilesHistogram;
        this.percentiles = percentiles;
        this.sla = sla;

        this.timeunit = ofNullable(timeunit).orElse(TimeUnit.SECONDS.name());
        this.record = record;
        this.timer = timer;
        this.registry = ofNullable(registry).orElse(globalRegistry);
    }

    @Override
    public List<String> validateInputs() {
        Validator<Object> metricNameValidation = of(null)
            .validate(a -> name != null || timer != null, "name and timer cannot be both null");

        return getErrorsFrom(
            metricNameValidation,
            integerStringValidation(bufferLength, "bufferLength"),
            integerStringValidation(percentilePrecision, "percentilePrecision"),
            durationStringValidation(maxValue, "maxValue"),
            durationStringValidation(minValue, "minValue"),
            durationStringValidation(record, "record"),
            durationStringValidation(expiry, "expiry"),
            percentilesListValidation(percentiles),
            slaListToDoublesValidation(sla)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            this.timer = ofNullable(timer).orElseGet(() -> this.retrieveTimer(registry));
            if (record != null) {
                timer.record(parseDuration(record));
                logger.info("Timer updated by " + record);
            }
            logTimerState(logger, timer, valueOf(timeunit));
            return ActionExecutionResult.ok(toOutputs(OUTPUT_TIMER, timer));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }

    private Timer retrieveTimer(MeterRegistry registry) {
        Timer.Builder builder = Timer.builder(requireNonNull(name))
            .description(description)
            .distributionStatisticBufferLength(parseIntOrNull(bufferLength))
            .distributionStatisticExpiry(parseDurationOrNull(expiry))
            .maximumExpectedValue(parseDurationOrNull(maxValue))
            .minimumExpectedValue(parseDurationOrNull(minValue))
            .percentilePrecision(parseIntOrNull(percentilePrecision))
            .publishPercentileHistogram(publishPercentilesHistogram)
            .publishPercentiles(parseMapOrNull(percentiles, MicrometerActionHelper::parsePercentilesList))
            .serviceLevelObjectives(parseMapOrNull(sla, MicrometerActionHelper::parseSlaListToDurations));

        ofNullable(tags).ifPresent(t -> builder.tags(t.toArray(new String[0])));

        return builder.register(registry);
    }
}
