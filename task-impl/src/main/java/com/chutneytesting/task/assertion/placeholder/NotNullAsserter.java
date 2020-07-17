package com.chutneytesting.task.assertion.placeholder;

import com.chutneytesting.task.spi.injectable.Logger;

public class NotNullAsserter implements PlaceholderAsserter {

    private static final String IS_NOTNULL = "$isNotNull";

    @Override
    public boolean canApply(String value) {
        return IS_NOTNULL.equals(value);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        return actual != null;
    }

}
