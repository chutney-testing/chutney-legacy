package com.chutneytesting.action.assertion.placeholder;

import com.chutneytesting.action.spi.injectable.Logger;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EqualDateAsserter implements PlaceholderAsserter {

    private static final String LOCAL_DATETIME_EQUAL = "$isEqualDate:";

    @Override
    public boolean canApply(String value) {
        return value.startsWith(LOCAL_DATETIME_EQUAL);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String date = expected.toString().substring(LOCAL_DATETIME_EQUAL.length());
        try {
            ZonedDateTime expectedDate = ZonedDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
            ZonedDateTime actualDate = ZonedDateTime.parse(actual.toString(), DateTimeFormatter.ISO_DATE_TIME);
            logger.info("Verify " + actualDate + " isEqual " + expectedDate);
            return actualDate.isEqual(expectedDate);
        } catch (DateTimeParseException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
