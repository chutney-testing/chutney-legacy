package com.chutneytesting.task.assertion.placeholder;

import com.chutneytesting.task.spi.injectable.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BeforeDateAsserter implements PlaceholderAsserter {

    private static final String LOCAL_DATETIME_BEFORE = "$isBeforeDate:";

    @Override
    public boolean canApply(String value) {
        return value.startsWith(LOCAL_DATETIME_BEFORE);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String date = expected.toString().substring(LOCAL_DATETIME_BEFORE.length());
        try {
            LocalDateTime expectedDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime actualDate = LocalDateTime.parse(actual.toString(), DateTimeFormatter.ISO_DATE_TIME);
            return actualDate.isBefore(expectedDate);
        } catch (DateTimeParseException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
