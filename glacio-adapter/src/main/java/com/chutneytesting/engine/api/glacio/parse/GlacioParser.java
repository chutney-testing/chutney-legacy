package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.ast.Step;
import java.util.Map;

public abstract class GlacioParser implements GlacioExecutableStepParser {

    @Override
    public final StepDefinition parseStep(Step step) {
        return new StepDefinition(
            parseStepName(step),
            parseStepTarget(step),
            parseTaskType(step),
            parseStepStrategy(step),
            parseTaskInputs(step),
            emptyList(),
            parseTaskOutputs(step)
        );
    }

    public abstract String parseTaskType(Step step);

    public String parseStepName(Step step) {
        return step.getText();
    }

    public Map<String, Object> parseTaskInputs(Step step) {
        return emptyMap();
    }

    public Map<String, Object> parseTaskOutputs(Step step) {
        return emptyMap();
    }

    public Target parseStepTarget(Step step) {
        return null;
    }

    public StepStrategyDefinition parseStepStrategy(Step step) {
        return null;
    }

}
