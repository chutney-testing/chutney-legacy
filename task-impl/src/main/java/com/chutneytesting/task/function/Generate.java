package com.chutneytesting.task.function;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Generate {
    private static final Random LONG_GENERATOR = new Random();

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public String randomLong() { return String.valueOf(LONG_GENERATOR.nextLong()); }

    public String randomInt(int bound) { return String.valueOf(LONG_GENERATOR.nextInt(bound)); }

    public String id(String prefix, int length) { return id(prefix, length, ""); }

    public String id(int length, String suffix) { return id("", length, suffix); }

    public String id(String prefix, int length, String suffix) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        int random = ThreadLocalRandom.current().nextInt(0, uuid.length() - length);;
        return prefix + uuid.substring(random, random+length) + suffix;
    }

}
