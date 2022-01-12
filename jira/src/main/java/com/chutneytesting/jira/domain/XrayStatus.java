package com.chutneytesting.jira.domain;

public enum XrayStatus {
    PASS("PASS"),
    FAIL("FAIL");

    public final String value;

    XrayStatus(String value) {
        this.value = value;
    }
}
