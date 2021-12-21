package com.chutneytesting.task.function;

import com.chutneytesting.task.spi.SpelFunction;
import com.chutneytesting.task.spi.time.DurationUnit;
import com.chutneytesting.tools.Try;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.LocaleUtils;

public class DateTimeFunctions {

    @SpelFunction
    public static Temporal date(String date, String... format) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_INSTANT;
        if (format.length > 0) {
            dateFormatter = DateTimeFormatter.ofPattern(format[0]);
        }
        return parseDateWithFormatter(date, dateFormatter);
    }

    @SpelFunction
    public static String currentTimeMillis() {
        return String.valueOf(System.currentTimeMillis());
    }

    @SpelFunction
    public static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    @SpelFunction
    public static DateTimeFormatter localDateFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    @SpelFunction
    public static DateTimeFormatter dateFormatter(String pattern, String locale) {
        return DateTimeFormatter.ofPattern(pattern, LocaleUtils.toLocale(locale));
    }

    @SpelFunction
    public static DateTimeFormatter isoDateFormatter(String type) {
        if (type != null) {
            switch (type.toUpperCase()) {
                case "INSTANT":
                    return DateTimeFormatter.ISO_INSTANT;
                case "ZONED_DATE_TIME":
                    return DateTimeFormatter.ISO_ZONED_DATE_TIME;
                case "DATE_TIME":
                    return DateTimeFormatter.ISO_DATE_TIME;
                case "DATE":
                    return DateTimeFormatter.ISO_DATE;
                case "TIME":
                    return DateTimeFormatter.ISO_TIME;
                case "LOCAL_DATE_TIME":
                    return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                case "LOCAL_DATE":
                    return DateTimeFormatter.ISO_LOCAL_DATE;
                case "LOCAL_TIME":
                    return DateTimeFormatter.ISO_LOCAL_TIME;
                case "OFFSET_DATE_TIME":
                    return DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                case "OFFSET_DATE":
                    return DateTimeFormatter.ISO_OFFSET_DATE;
                case "OFFSET_TIME":
                    return DateTimeFormatter.ISO_OFFSET_TIME;
                case "ORDINAL_DATE":
                    return DateTimeFormatter.ISO_ORDINAL_DATE;
                case "ISO_WEEK_DATE":
                    return DateTimeFormatter.ISO_WEEK_DATE;
                case "BASIC_DATE":
                    return DateTimeFormatter.BASIC_ISO_DATE;
                case "RFC_DATE_TIME":
                    return DateTimeFormatter.RFC_1123_DATE_TIME;
            }
        }
        throw new IllegalArgumentException("Unknown date time formatter type [" + type + "]");
    }

    @SpelFunction
    public static TemporalAmount timeAmount(String text) {
        AtomicReference<TemporalAmount> ta = new AtomicReference<>();

        Try.exec(() -> Duration.ofMillis(com.chutneytesting.task.spi.time.Duration.parseToMs(text)))
            .ifSuccess(ta::set);

        if (ta.get() == null) {
            Try.exec(() -> Duration.parse(text)).ifSuccess(ta::set);

            if (ta.get() == null) {
                Try.exec(() -> Period.parse(text)).ifSuccess(ta::set);

                if (ta.get() == null) {
                    throw new IllegalArgumentException("Cannot parse [" + text + "] as amount of time");
                }
            }
        }

        return ta.get();
    }

    @SpelFunction
    public static ChronoUnit timeUnit(String unit) {
        AtomicReference<ChronoUnit> cu = new AtomicReference<>();

        Try.exec(() -> DurationUnit.parse(unit).timeUnit.toChronoUnit())
            .ifSuccess(cu::set);

        if (cu.get() == null) {
            Try.exec(() -> ChronoUnit.valueOf(unit.toUpperCase())).ifSuccess(cu::set);

            if (cu.get() == null) {
                throw new IllegalArgumentException("Cannot parse [" + unit + "] as unit of time");
            }
        }

        return cu.get();
    }

    private static Temporal parseDateWithFormatter(String date, DateTimeFormatter dateFormatter) {
        return (Temporal) dateFormatter.parseBest(date, ZonedDateTime::from, LocalDateTime::from, LocalDate::from, Instant::from);
    }
}
