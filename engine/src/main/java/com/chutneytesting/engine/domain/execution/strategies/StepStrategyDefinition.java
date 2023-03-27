package com.chutneytesting.engine.domain.execution.strategies;

public class StepStrategyDefinition {
    public final String type;
    public final StrategyProperties strategyProperties;

    public StepStrategyDefinition(String type, StrategyProperties strategyProperties) {
        this.type = type;
        this.strategyProperties = strategyProperties;
    }

    @Override
    public String toString() {
        return "StepStrategyDefinition{" +
            "type='" + type + '\'' +
            ", strategyProperties=" + strategyProperties +
            '}';
    }
}
