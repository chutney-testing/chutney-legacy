package com.chutneytesting.design.domain.campaign;

public enum FREQUENCY {
    HOURLY("Hourly"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    final String label;

    FREQUENCY(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
