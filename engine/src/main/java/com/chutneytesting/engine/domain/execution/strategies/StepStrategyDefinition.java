package com.chutneytesting.engine.domain.execution.strategies;

import java.util.Objects;

public class StepStrategyDefinition {
    public final String type;
    public final StrategyProperties strategyProperties;

    public StepStrategyDefinition(String type, StrategyProperties strategyProperties) {
        this.type = type;
        this.strategyProperties = strategyProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepStrategyDefinition that = (StepStrategyDefinition) o;

        if (!Objects.equals(type, that.type)) return false;
        return Objects.equals(strategyProperties, that.strategyProperties);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (strategyProperties != null ? strategyProperties.hashCode() : 0);
        return result;
    }
}
