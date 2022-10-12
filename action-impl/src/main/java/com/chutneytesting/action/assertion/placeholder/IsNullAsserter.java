package com.chutneytesting.action.assertion.placeholder;

import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Collection;

public class IsNullAsserter implements PlaceholderAsserter {

    private static final String IS_NULL = "$isNull";

    @Override
    public boolean canApply(String value) {
        return IS_NULL.equals(value);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        logger.info("Verify " + actual + " == null");
        if (actual == null) {
            return true;
        }

        if (actual instanceof Collection) {
            return ((Collection<?>) actual).isEmpty();
        }

        return false;
    }
}
