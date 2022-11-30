package com.chutneytesting.tools;

import java.text.DecimalFormat;

public class NotEnoughMemoryException extends RuntimeException {

    public NotEnoughMemoryException(long usedMemory, long maxMemory, String customMsg) {
        super(
            "Running step was stopped to prevent application crash. "
                + toMegaByte(usedMemory) + "MB memory used of " + toMegaByte(maxMemory) + "MB max."
                + "\n" + "Current step may not be the cause."
                + "\n" + customMsg
        );
    }

    private static String toMegaByte(long value) {
        return new DecimalFormat("#.##").format(value / (double)(1024 * 1024));
    }

}
