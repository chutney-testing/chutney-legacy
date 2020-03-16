package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.ast.Step;
import java.util.Map;

public interface GlacioExecutableStepParser {

    Integer priority();
    boolean couldParse(String stepText);

    default String parseStepName(Step step) {
        return step.getText();
    }

    String parseTaskType(Step step);

    default Map<String, Object> parseTaskInputs(Step step) {
        return emptyMap();
    }

    default Map<String, Object> parseTaskOutputs(Step step) {
        return emptyMap();
    }

    default Target parseStepTarget(Step step) {
        return null;
    }

    default StepStrategyDefinition parseStepStrategy(Step step) {
        return null;
    }

    default StepDefinition parseStep(Step step) {
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
}
