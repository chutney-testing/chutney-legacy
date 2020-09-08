package com.chutneytesting.task.assertion.placeholder;

import com.chutneytesting.task.spi.injectable.Logger;
import java.text.NumberFormat;
import java.text.ParseException;

public class LessThanAsserter implements PlaceholderAsserter {

    private static final String IS_LESS_THAN = "$isLessThan:";
    private static final NumberFormat nb = NumberFormat.getInstance();

    @Override
    public boolean canApply(String value) {
        return value.startsWith(IS_LESS_THAN);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String expect = expected.toString().substring(IS_LESS_THAN.length());
        try {
            Number numActual = nb.parse(actual.toString().replaceAll(" ", ""));
            Number numExpected = nb.parse(expect.replaceAll(" ", ""));
            return numActual.doubleValue() < numExpected.doubleValue();
        } catch (ParseException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
