package com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.model.Step;
import java.util.List;
import java.util.Locale;

public interface IParseBusinessStep {

    /** TODO put description here **/
    StepDefinition mapToStepDefinition(Locale lang, String environment, Step step, List<StepDefinition> subSteps);

}
