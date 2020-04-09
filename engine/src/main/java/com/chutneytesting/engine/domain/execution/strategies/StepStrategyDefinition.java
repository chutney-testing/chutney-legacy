package com.chutneytesting.engine.domain.execution.strategies;

import java.util.Objects;

public class StepStrategyDefinition {
    public final String type;
    public final StrategyProperties strategyProperties;

    public StepStrategyDefinition(String type, StrategyProperties strategyProperties) {
        this.type = type;
        this.strategyProperties = strategyProperties;
    }
}
