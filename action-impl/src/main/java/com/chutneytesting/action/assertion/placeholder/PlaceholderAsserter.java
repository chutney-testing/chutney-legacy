package com.chutneytesting.action.assertion.placeholder;

import com.chutneytesting.action.spi.injectable.Logger;

public interface PlaceholderAsserter {

    boolean canApply(String value);

    boolean assertValue(Logger logger, Object actual, Object expected);

}
