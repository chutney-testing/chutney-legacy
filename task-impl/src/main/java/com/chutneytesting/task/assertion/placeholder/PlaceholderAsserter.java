package com.chutneytesting.task.assertion.placeholder;

import com.chutneytesting.task.spi.injectable.Logger;

public interface PlaceholderAsserter {

    boolean canApply(String value);

    boolean assertValue(Logger logger, Object actual, Object expected);

}
