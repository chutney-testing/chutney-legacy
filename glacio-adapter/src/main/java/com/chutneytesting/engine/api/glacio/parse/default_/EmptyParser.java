package com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.api.glacio.parse.InputsParser;
import com.chutneytesting.engine.api.glacio.parse.OutputsParser;
import com.chutneytesting.engine.api.glacio.parse.StrategyParser;
import com.chutneytesting.engine.api.glacio.parse.TargetParser;
import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.ast.Step;
import java.util.Map;

public final class EmptyParser implements InputsParser, OutputsParser, TargetParser, StrategyParser {

    public static final EmptyParser instance = new EmptyParser();

    private EmptyParser() {
    }

    @Override
    public Map<String, Object> parseTaskInputs(Step step) {
        return emptyMap();
    }

    @Override
    public Map<String, Object> parseTaskOutputs(Step step) {
        return emptyMap();
    }

    @Override
    public StepStrategyDefinition parseStepStrategy(Step step) {
        return null;
    }

    @Override
    public Target parseStepTarget(Step step) {
        return null;
    }
}
