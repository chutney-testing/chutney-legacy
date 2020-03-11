package com.chutneytesting.engine.api.glacio;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.ast.Feature;
import com.github.fridujo.glacio.ast.RootStep;
import com.github.fridujo.glacio.ast.Scenario;
import com.github.fridujo.glacio.ast.Step;
import com.github.fridujo.glacio.parsing.charstream.CharStream;
import com.github.fridujo.glacio.parsing.i18n.GherkinLanguages;
import com.github.fridujo.glacio.parsing.lexer.Lexer;
import com.github.fridujo.glacio.parsing.parser.AstParser;
import java.util.List;
import java.util.stream.Collectors;

public class GlacioAdapter {

    public List<StepDefinition> toChutneyStepDefinition(String text) {
        Feature feature = this.toFeature(text);
        List<Scenario> scenarios = feature.getScenarios();

        return scenarios.stream()
            .map(s -> toStepDefinition(feature.getName(), s))
            .collect(Collectors.toList());
    }

    private static StepDefinition toStepDefinition(String featureName, Scenario scenario) {
        String name = featureName + " - " + scenario.getName();

        List<StepDefinition> subSteps = scenario.getSteps().stream()
            .map(GlacioAdapter::toStepDefinition)
            .collect(Collectors.toList());

        return new StepDefinition(name, null, "", null, emptyMap(), subSteps, emptyMap());
    }

    private static StepDefinition toStepDefinition(RootStep rootStep) {
        String name = rootStep.getKeyword().getLiteral() + rootStep.getText();

        List<StepDefinition> subSteps = rootStep.getSubsteps().stream()
            .map(GlacioAdapter::toStepDefinition)
            .collect(Collectors.toList());

        return new StepDefinition(name, null, "", null, emptyMap(), subSteps, emptyMap());
    }

    private static StepDefinition toStepDefinition(Step step) {
        if (step.getSubsteps().size() == 0) {
            return delegateToTask(step);
        }

        List<StepDefinition> subSteps = step.getSubsteps().stream()
            .map(GlacioAdapter::toStepDefinition)
            .collect(Collectors.toList());

        return new StepDefinition(step.getText(), null, "", null, emptyMap(), subSteps, emptyMap());
    }

    private static StepDefinition delegateToTask(Step step) {
        return new StepDefinition(step.getText(), null, "", null, emptyMap(), emptyList(), emptyMap());
    }

    private Feature toFeature(String text) {
        Lexer lexer = new Lexer(new CharStream(text));
        AstParser astParser = new AstParser(lexer, GherkinLanguages.load());
        return astParser.parseFeature();
    }
}
