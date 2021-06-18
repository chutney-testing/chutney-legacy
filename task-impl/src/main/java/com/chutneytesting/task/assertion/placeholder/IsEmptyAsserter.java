package com.chutneytesting.task.assertion.placeholder;

import com.chutneytesting.task.spi.injectable.Logger;
import net.minidev.json.JSONArray;

public class IsEmptyAsserter implements PlaceholderAsserter {

    private static final String IS_EMPTY = "$isEmpty";

    @Override
    public boolean canApply(String value) {
        return IS_EMPTY.equals(value);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        return assertValue(logger, actual);
    }

    public boolean assertValue(Logger logger, Object actual) {
        logger.info("Verify " + actual + " is empty");
        if (actual instanceof JSONArray) {
            JSONArray actualArray = (JSONArray) actual;
            return actualArray.stream().map(e -> assertValue(logger, e)).reduce((a, b) -> a && b).orElse(true);
        } else {
            return actual.toString().isEmpty();
        }
    }
}
