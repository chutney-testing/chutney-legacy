package com.chutneytesting.engine.domain.execution.strategies;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.execution.engine.step.Step;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StepExecutionStrategies {
    private final Map<String, StepExecutionStrategy> strategies;

    public StepExecutionStrategies() {
        this(new HashSet<>());
    }

    public StepExecutionStrategies(Set<StepExecutionStrategy> strategies) {

        if (strategies == null) {
            strategies = new HashSet<>();
        }

        this.strategies = strategies.stream().collect(Collectors.toMap(StepExecutionStrategy::getType
            , Function.identity()
            , (x, y) -> {
                throw new IllegalStateException("Found multiple implementations for strategy: '" + x.getType() + "': " + x.getClass() + " | " + y.getClass());
            }));
    }

    public StepExecutionStrategy buildStrategyFrom(Step step) {
        return step.strategy()
            .map(this::findStrategy)
            .orElse(DefaultStepExecutionStrategy.instance);

    }

    private StepExecutionStrategy findStrategy(StepStrategyDefinition strategyDefinition) {
        if (strategyDefinition.type.isEmpty()) {
            return DefaultStepExecutionStrategy.instance;
        }

        return ofNullable(strategies.get(strategyDefinition.type))
            .orElseThrow(() -> new IllegalStateException("Could not find strategy of type: '" + strategyDefinition.type + "'"));
    }
}
