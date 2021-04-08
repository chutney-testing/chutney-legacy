package com.chutneytesting.design.domain.campaign;

public enum FREQUENCY {
    HOURLY("Hourly"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    EMPTY("");

    public final String label;

    FREQUENCY(String label) {
        this.label = label;
    }

    public static FREQUENCY tofrequency(String actualLabel) {
        for (FREQUENCY frequency : values())
            if (frequency.label.equals(actualLabel)) return frequency;
        return EMPTY;
    }

    @Override
    public String toString() {
        return label;
    }
}
