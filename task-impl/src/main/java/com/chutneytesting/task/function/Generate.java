package com.chutneytesting.task.function;

import java.util.Random;
import java.util.UUID;

public class Generate {
    private static final Random LONG_GENERATOR = new Random();

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public String randomLong() { return String.valueOf(LONG_GENERATOR.nextLong()); }

    public String randomInt(int bound) { return String.valueOf(LONG_GENERATOR.nextInt(bound)); }

}
