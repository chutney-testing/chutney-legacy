package com.chutneytesting.task.function;

import com.chutneytesting.task.spi.SpelFunction;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

public class DateFunction {

    @SpelFunction
    public static Temporal date(String date, String...format) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_INSTANT;
        if(format.length > 0) {
            dateFormatter = DateTimeFormatter.ofPattern(format[0]);
        }
        return parseDateWithFormatter(date, dateFormatter);
    }

    @SpelFunction
    public static String currentTimeMillis() {
        return String.valueOf(System.currentTimeMillis());
    }

     public static Temporal parseDateWithFormatter(String date, DateTimeFormatter dateFormatter) {
        return (Temporal)dateFormatter.parseBest(date, ZonedDateTime::from, LocalDateTime::from, LocalDate::from, Instant::from);
    }
}
