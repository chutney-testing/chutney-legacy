package com.chutneytesting.task.assertion.placeholder;

import com.chutneytesting.task.spi.injectable.Logger;

public class IsNullAsserter implements PlaceholderAsserter {

    private static final String IS_NULL = "$isNull";

    @Override
    public boolean canApply(String value) {
        return IS_NULL.equals(value);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        return actual == null;
    }

}
