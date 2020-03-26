package com.chutneytesting.engine.api.glacio;

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
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GlacioAdapter {

    private ExecutableStepFactory executableStepFactory;

    public GlacioAdapter(ExecutableStepFactory executableStepFactory) {
        this.executableStepFactory = executableStepFactory;
    }

    public List<StepDefinition> toChutneyStepDefinition(String text) {
        Feature feature = this.toFeature(text);
        List<Scenario> scenarios = feature.getScenarios();

        return scenarios.stream()
            .map(s -> toStepDefinition(feature.getName(), Locale.ENGLISH, s))
            .collect(Collectors.toList());
    }

    private StepDefinition toStepDefinition(String featureName, Locale lang, Scenario scenario) {
        String name = featureName + " - " + scenario.getName();

        List<StepDefinition> subSteps = scenario.getSteps().stream()
            .map(rootStep -> toStepDefinition(lang, rootStep))
            .collect(Collectors.toList());

        return buildNonExecutableStep(subSteps, name);
    }

    private StepDefinition toStepDefinition(Locale lang, RootStep rootStep) {
        if (this.executableStepFactory.isExecutableStep(lang, rootStep)) {
            return this.executableStepFactory.build(lang, rootStep);
        } else {
            List<StepDefinition> subSteps = toStepSubStepsDefinitions(lang, rootStep);
            String name = rootStep.getKeyword().getLiteral() + rootStep.getText();
            return buildNonExecutableStep(subSteps, name);
        }
    }

    private StepDefinition toStepDefinition(Locale lang, Step step) {
        if (this.executableStepFactory.isExecutableStep(lang, step)) {
            return this.executableStepFactory.build(lang, step);
        } else {
            List<StepDefinition> subSteps = toStepSubStepsDefinitions(lang, step);
            return buildNonExecutableStep(subSteps, step.getText());
        }
    }

    private StepDefinition buildNonExecutableStep(List<StepDefinition> subSteps, String text) {
        return new StepDefinition(text, null, "", null, emptyMap(), subSteps, emptyMap());
    }

    private List<StepDefinition> toStepSubStepsDefinitions(Locale lang, Step step) {
        return step.getSubsteps().stream()
            .map(subStep -> toStepDefinition(lang, subStep))
            .collect(Collectors.toList());
    }

    private Feature toFeature(String text) {
        Lexer lexer = new Lexer(new CharStream(text));
        AstParser astParser = new AstParser(lexer, GherkinLanguages.load());
        return astParser.parseFeature();
    }
}
