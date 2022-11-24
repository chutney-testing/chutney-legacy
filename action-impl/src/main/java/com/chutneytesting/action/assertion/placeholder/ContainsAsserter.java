package com.chutneytesting.action.assertion.placeholder;

import com.chutneytesting.action.spi.injectable.Logger;

public class ContainsAsserter implements PlaceholderAsserter {

    private static final String CONTAINS = "$contains:";

    @Override
    public boolean canApply(String value) {
        return value.startsWith(CONTAINS);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String toFind = expected.toString().substring(CONTAINS.length());
        logger.info("Verify " + actual.toString() + " contains " + toFind);
        return actual.toString().contains(toFind);
    }

}
