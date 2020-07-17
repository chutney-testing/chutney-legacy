package com.chutneytesting.task.assertion.placeholder;

import com.chutneytesting.task.spi.injectable.Logger;

public class ContainsAsserter implements PlaceholderAsserter {

    private static final String CONTAINS = "$contains:";

    @Override
    public boolean canApply(String value) {
        return value.startsWith(CONTAINS);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String toFind = expected.toString().substring(CONTAINS.length());
        return actual.toString().contains(toFind);
    }

}
