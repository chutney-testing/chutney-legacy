package com.chutneytesting.tools;

public class ChutneyMemoryInfo {

    public static final long MAX_MEMORY = Runtime.getRuntime().maxMemory(); // Fixed at startup
    public static final int MINIMUM_MEMORY_PERCENTAGE_REQUIRED = 5;
    public static final long MINIMUM_MEMORY_REQUIRED = (MAX_MEMORY / 100) * MINIMUM_MEMORY_PERCENTAGE_REQUIRED;

    public static long availableMemory() {
        return MAX_MEMORY - usedMemory();
    }

    public static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static boolean hasEnoughAvailableMemory() {
        return availableMemory() > MINIMUM_MEMORY_REQUIRED;
    }
}
