package com.chutneytesting.action.assertion.placeholder;

import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.injectable.Logger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import wiremock.net.minidev.json.JSONArray;

public class ValueArrayAsserter implements PlaceholderAsserter {

    private static final Pattern IS_VALUE = Pattern.compile("^\\$value(\\[(?<index>[0-9]+)])?:(?<expected>.+)$");
    private static final Predicate<String> IS_VALUE_TEST = IS_VALUE.asMatchPredicate();

    @Override
    public boolean canApply(String value) {
        return IS_VALUE_TEST.test(value);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        if (actual instanceof JSONArray) {
            Matcher matcher = IS_VALUE.matcher(expected.toString());
            if (matcher.matches()) {
                JSONArray actualArray = (JSONArray) actual;
                AtomicInteger arrayIndex = new AtomicInteger(0);
                ofNullable(matcher.group("index")).ifPresent(s -> arrayIndex.set(Integer.parseInt(s)));
                String expect = matcher.group("expected");

                try {
                    String act = actualArray.get(arrayIndex.get()).toString();
                    logger.info("Verify " + expect + " = " + act);
                    return expect.equals(act);
                } catch (IndexOutOfBoundsException ioobe) {
                    logger.error("Index array is out of bound : " + ioobe);
                }
            } else {
                logger.error("Expected value don't match asserter pattern");
            }
        } else {
            logger.error("Actual value is not an array");
        }

        return false;
    }
}
