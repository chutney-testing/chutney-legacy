package com.chutneytesting.design.domain.compose;

public class ParentStepId {

    public String id;
    public String name;
    public boolean isScenario;

    public ParentStepId(String id, String name, boolean isScenario) {
        this.id = id;
        this.name = name;
        this.isScenario = isScenario;
    }
}

