package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.spi.validation.Validator.of;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

final class MicrometerTaskHelper {

    static Validator<String> durationStringValidation(String timeout, String label) {
        return of(timeout)
            .validate(s -> s == null || com.chutneytesting.task.spi.time.Duration.parseToMs(s) >= Long.MIN_VALUE , noException -> true, label + " is not parsable");
    }

    static Validator<String> integerStringValidation(String toVerify, String inputLabel) {
        return of(toVerify)
            .validate(s -> s == null || parseInt(s) >= Integer.MIN_VALUE, noException -> true, inputLabel + " parsing");
    }

    static Validator<String> doubleStringValidation(String toVerify, String inputLabel) {
        return of(toVerify)
            .validate(s -> s == null || parseDouble(s) >= Double.MIN_VALUE, noException -> true, inputLabel + " parsing");
    }

    static Integer parseIntOrNull(String str) {
        return parseMapOrNull(str, Integer::parseInt);
    }

    static Double parseDoubleOrNull(String str) {
        return parseMapOrNull(str, Double::parseDouble);
    }

    static Duration parseDurationOrNull(String str) {
        return parseMapOrNull(str, MicrometerTaskHelper::parseDuration);
    }

    static <T, U> T parseMapOrNull(U nullable, Function<U, T> mapfunction) {
        return ofNullable(nullable).map(mapfunction).orElse(null);
    }

    static Map<String, Object> toOutputs(String key, Object value) {
        if (value != null) {
            Map<String, Object> outputs = new HashMap<>();
            outputs.put(key, value);
            return outputs;
        }
        return emptyMap();
    }

    static double[] parsePercentilesList(String percentiles) {
        return splitStringList(percentiles)
            .mapToDouble(Double::parseDouble)
            .toArray();
    }

    static Duration[] parseSlaListToDurations(String sla) {
        return splitStringList(sla)
            .map(Duration::parse)
            .toArray(Duration[]::new);
    }

    static double[] parseSlaListToDoubles(String sla) {
        return splitStringList(sla)
            .mapToDouble(Double::parseDouble)
            .toArray();
    }

    static void logTimerState(Logger logger, Timer timer, TimeUnit timeunit) {
        logger.info("Timer current total time is " + timer.totalTime(timeunit) + " " + timeunit);
        logger.info("Timer current max time is " + timer.max(timeunit) + " " + timeunit);
        logger.info("Timer current mean time is " + timer.mean(timeunit) + " " + timeunit);
        logger.info("Timer current count is " + timer.count());
    }

    static Validator<String> percentilesListValidation(String percentilesList) {
        return of(percentilesList)
            .validate(s -> s == null || parsePercentilesList(s) != null, "Cannot parse percentils list");
    }

    static Validator<String> slaListToDoublesValidation(String sla) {
        return of(sla)
            .validate(s -> s == null || parseSlaListToDoubles(s) != null, "Cannot parse sla list");
    }

    static Duration parseDuration(String duration) {
        return Duration.of(com.chutneytesting.task.spi.time.Duration.parse(duration).toMilliseconds(), ChronoUnit.MILLIS);
    }

    private static Stream<String> splitStringList(String list) {
        return Arrays.stream(list.split(",")).map(String::trim);
    }
}
