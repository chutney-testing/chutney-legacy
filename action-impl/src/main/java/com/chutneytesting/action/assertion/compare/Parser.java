package com.chutneytesting.action.assertion.compare;

import com.chutneytesting.action.spi.injectable.Logger;

public class Parser {

     static double actualDouble;
     static double expectedDouble;

    public static boolean isParsableFrom(Logger logger, String actual, String expected) {

        try {
            actualDouble = Double.parseDouble(actual);
        } catch (NumberFormatException nfe) {
            logger.error("[" + actual + "] is Not Numeric");
        }

        try {
            expectedDouble = Double.parseDouble(expected);
        } catch (NumberFormatException nfe) {
            logger.error("[" + expected + "] is Not Numeric");
            return false;
        }

        return true;
    }
}
