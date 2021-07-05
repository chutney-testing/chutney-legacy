package com.chutneytesting.design.domain.campaign;

public enum Frequency {

    HOURLY("Hourly"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    EMPTY("");

    public final String label;

    Frequency(String label) {
        this.label = label;
    }

    public static Frequency toFrequency(String label) {
        for (Frequency frequency : values()) {
            if (frequency.label.equals(label)) return frequency;
        }
        return EMPTY;
    }

    @Override
    public String toString() {
        return label;
    }
}
