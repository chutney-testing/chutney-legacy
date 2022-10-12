package com.chutneytesting.action.assertion.placeholder;

import com.chutneytesting.action.spi.injectable.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchesStringAsserter implements PlaceholderAsserter {

    private static final String MATCHES_STRINGS = "$matches:";

    @Override
    public boolean canApply(String value) {
        return value.startsWith(MATCHES_STRINGS);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String patternToFound = expected.toString().substring(MATCHES_STRINGS.length());
        Pattern pattern = Pattern.compile(patternToFound);
        Matcher matcher = pattern.matcher(actual.toString());
        logger.info("Verify " + actual.toString() + " matches " + patternToFound);
        return matcher.matches();
    }

}
