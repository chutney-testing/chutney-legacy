package com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.ast.Step;
import java.util.Map;

public final class EmptyParser {

    public static final StepParser<Map<String, Object>> emptyMapParser = new EmptyMapParser();
    public static final StepParser<StepStrategyDefinition> noStrategyParser = new NoStrategyParser();
    public static final StepParser<Target> noTargetParser = new NoTargetParser();

    private EmptyParser() {
    }

    private static class EmptyMapParser implements StepParser<Map<String, Object>> {
        @Override
        public Map<String, Object> parseStep(Step step) {
            return emptyMap();
        }
    }

    private static class NoStrategyParser implements StepParser<StepStrategyDefinition> {
        @Override
        public StepStrategyDefinition parseStep(Step step) {
            return null;
        }
    }

    private static class NoTargetParser implements StepParser<Target> {
        @Override
        public Target parseStep(Step step) {
            return TargetImpl.NONE;
        }
    }
}
