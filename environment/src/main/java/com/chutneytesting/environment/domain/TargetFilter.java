package com.chutneytesting.environment.domain;

public class TargetFilter {
    public final String name;
    public final String environment;

    public TargetFilter(String name, String environment) {
        this.name = name;
        this.environment = environment;
    }
}
