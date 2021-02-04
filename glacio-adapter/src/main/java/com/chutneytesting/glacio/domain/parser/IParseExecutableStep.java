package com.chutneytesting.glacio.domain.parser;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.model.Step;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface IParseExecutableStep {

    /** TODO put description here **/
    Map<Locale, Set<String>> keywords();

    /** TODO put description here **/
    StepDefinition mapToStepDefinition(ParsingContext context, Step step, StepStrategyDefinition stepStrategyDefinition);

}
