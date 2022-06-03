package com.chutneytesting.glacio.domain.parser;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.github.fridujo.glacio.model.Step;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface IParseExecutableStep {

    /** TODO put description here **/
    Map<Locale, Set<String>> keywords();

    /** TODO put description here **/
    StepDefinitionDto mapToStepDefinition(ParsingContext context, Step step, StepDefinitionDto.StepStrategyDefinitionDto stepStrategyDefinition);

}
