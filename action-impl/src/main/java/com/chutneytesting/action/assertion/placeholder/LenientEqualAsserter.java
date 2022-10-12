package com.chutneytesting.action.assertion.placeholder;

import static com.chutneytesting.action.common.JsonUtils.lenientEqual;

import com.chutneytesting.action.spi.injectable.Logger;
import com.jayway.jsonpath.JsonPath;

public class LenientEqualAsserter implements PlaceholderAsserter {

    private static final String IS_LENIENT_EQUAL = "$lenientEqual:";

    @Override
    public boolean canApply(String value) {
        return value.startsWith(IS_LENIENT_EQUAL);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String expect = expected.toString().substring(IS_LENIENT_EQUAL.length());
        Object expectedRead = JsonPath.parse(expect).read("$");
        return lenientEqual(actual, expectedRead, true);
    }
}
