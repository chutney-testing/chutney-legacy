package com.chutneytesting.tools;

public class ChutneyMemoryInfo {

    private static final long MAX_MEMORY = Runtime.getRuntime().maxMemory(); // Fixed at startup
    private static final int MINIMUM_MEMORY_PERCENTAGE_REQUIRED = 5;
    private static final long MINIMUM_MEMORY_REQUIRED = (MAX_MEMORY / 100) * MINIMUM_MEMORY_PERCENTAGE_REQUIRED;

    public static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static boolean hasEnoughAvailableMemory() {
        return availableMemory() > MINIMUM_MEMORY_REQUIRED;
    }

    public static long maxMemory() {
        return MAX_MEMORY;
    }

    private static long availableMemory() {
        return MAX_MEMORY - usedMemory();
    }
}
