package com.chutneytesting.scenario.domain;

public class ParentStepId {

    public final String id;
    public final String name;
    public final boolean isScenario;

    public ParentStepId(String id, String name, boolean isScenario) {
        this.id = id;
        this.name = name;
        this.isScenario = isScenario;
    }
}

