package com.chutneytesting.task.micrometer;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

final class MicrometerTaskHelper {

    static MeterRegistry checkRegistry(MeterRegistry registry) {
        return ofNullable(registry).orElse(globalRegistry);
    }

    static Integer checkIntOrNull(String str) {
        return checkMapOrNull(str, Integer::parseInt);
    }

    static Long checkLongOrNull(String str) {
        return checkMapOrNull(str, Long::parseLong);
    }

    static Double checkDoubleOrNull(String str) {
        return checkMapOrNull(str, Double::parseDouble);
    }

    static Duration checkDurationOrNull(String str) {
        return checkMapOrNull(str, MicrometerTaskHelper::parseDuration);
    }

    static TimeUnit checkTimeUnit(String str) {
        return ofNullable(str).map(TimeUnit::valueOf).orElse(TimeUnit.SECONDS);
    }

    static <T, U> T checkMapOrNull(U nullable, Function<U, T> mapfunction) {
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
            .map(MicrometerTaskHelper::parseDuration)
            .toArray(Duration[]::new);
    }

    static long[] parseSlaListToLongs(String sla) {
        return splitStringList(sla)
            .mapToLong(Long::parseLong)
            .toArray();
    }

    private static Duration parseDuration(String duration) {
        return Duration.of(com.chutneytesting.task.spi.time.Duration.parse(duration).toMilliseconds(), ChronoUnit.MILLIS);
    }

    private static Stream<String> splitStringList(String list) {
        return Arrays.stream(list.split(",")).map(String::trim);
    }
}
