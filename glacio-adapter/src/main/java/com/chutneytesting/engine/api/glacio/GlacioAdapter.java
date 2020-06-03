package com.chutneytesting.engine.api.glacio;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

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
import java.util.Locale;
import java.util.stream.Collectors;

public class GlacioAdapter {

    public static final String DEFAULT_ENV = "ENV";

    private ExecutableStepFactory executableStepFactory;

    public GlacioAdapter(ExecutableStepFactory executableStepFactory) {
        this.executableStepFactory = executableStepFactory;
    }

    public List<StepDefinition> toChutneyStepDefinition(String text) {
        return toChutneyStepDefinition(text, DEFAULT_ENV);
    }

    public List<StepDefinition> toChutneyStepDefinition(String text, String environment) {
        Feature feature = this.toFeature(text);
        List<Scenario> scenarios = feature.getScenarios();

        return scenarios.stream()
            .map(s -> toStepDefinition(
                feature.getName(),
                feature.getLanguage().map(ps -> new Locale(ps.getValue())).orElse(Locale.ENGLISH),
                ofNullable(environment).orElse(DEFAULT_ENV),
                s)
            )
            .collect(Collectors.toList());
    }

    private StepDefinition toStepDefinition(String featureName, Locale lang, String environment, Scenario scenario) {
        String name = featureName + " - " + scenario.getName();

        List<StepDefinition> subSteps = scenario.getSteps().stream()
            .map(rootStep -> toStepDefinition(lang, environment, rootStep))
            .collect(Collectors.toList());

        return buildNonExecutableStep(subSteps, environment, name);
    }

    private StepDefinition toStepDefinition(Locale lang, String environment, RootStep rootStep) {
        if (this.executableStepFactory.isExecutableStep(lang, rootStep)) {
            return this.executableStepFactory.build(lang, environment, rootStep);
        } else {
            List<StepDefinition> subSteps = toStepSubStepsDefinitions(lang, environment, rootStep);
            String name = rootStep.getKeyword().getLiteral() + rootStep.getText();
            return buildNonExecutableStep(subSteps, environment, name);
        }
    }

    private StepDefinition toStepDefinition(Locale lang, String environment, Step step) {
        if (this.executableStepFactory.isExecutableStep(lang, step)) {
            return this.executableStepFactory.build(lang, environment, step);
        } else {
            List<StepDefinition> subSteps = toStepSubStepsDefinitions(lang, environment, step);
            return buildNonExecutableStep(subSteps, environment, step.getText());
        }
    }

    private StepDefinition buildNonExecutableStep(List<StepDefinition> subSteps, String environment, String text) {
        return new StepDefinition(text, null, "", null, emptyMap(), subSteps, emptyMap(), environment);
    }

    private List<StepDefinition> toStepSubStepsDefinitions(Locale lang, String environment, Step step) {
        return step.getSubsteps().stream()
            .map(subStep -> toStepDefinition(lang, environment, subStep))
            .collect(Collectors.toList());
    }

    private Feature toFeature(String text) {
        Lexer lexer = new Lexer(new CharStream(text));
        AstParser astParser = new AstParser(lexer, GherkinLanguages.load());
        return astParser.parseFeature();
    }
}
