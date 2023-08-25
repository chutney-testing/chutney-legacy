package com.chutneytesting.tools;

import com.chutneytesting.action.spi.SpelFunction;
import java.util.Random;
import java.util.UUID;

public class TestFunctions {
    private static final Random RANDOM_GENERATOR = new Random();

    @SpelFunction
    public static String randomID() {
        return UUID.randomUUID().toString();
    }

    @SpelFunction
    public static String randomInt(int bound) {
        return String.valueOf(RANDOM_GENERATOR.nextInt(bound));
    }
}
