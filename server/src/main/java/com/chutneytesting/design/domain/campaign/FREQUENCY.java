package com.chutneytesting.design.domain.campaign;

public enum FREQUENCY {
    HOURLY("hourly"),
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    final String label;

    FREQUENCY(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
