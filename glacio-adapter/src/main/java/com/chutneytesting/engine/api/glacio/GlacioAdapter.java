package com.chutneytesting.engine.api.glacio;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.model.Example;
import com.github.fridujo.glacio.model.Feature;
import com.github.fridujo.glacio.model.Step;
import com.github.fridujo.glacio.parsing.i18n.GherkinLanguages;
import com.github.fridujo.glacio.parsing.model.ModelParser;
import com.github.fridujo.glacio.parsing.model.StringSource;
import java.io.UncheckedIOException;
import java.net.URI;
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
        List<Example> examples = feature.getExamples();

        return examples.stream()
            .map(s -> toStepDefinition(
                feature.getName(),
                new Locale(feature.getLanguage().getCode()),
                ofNullable(environment).orElse(DEFAULT_ENV),
                s)
            )
            .collect(Collectors.toList());
    }

    private StepDefinition toStepDefinition(String featureName, Locale lang, String environment, Example example) {
        String name = featureName + " - " + example.getName();

        List<StepDefinition> subSteps = example.getSteps().stream()
            .map(rootStep -> toStepDefinition(lang, environment, rootStep))
            .collect(Collectors.toList());

        return buildNonExecutableStep(subSteps, environment, name);
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

        StringSource source = new TextStringSource(text);
        ModelParser parser = new ModelParser(GherkinLanguages.load());
        return parser.parse(source);
    }

    private static class TextStringSource implements StringSource {
        private final String content;

        private TextStringSource(String content) {
            this.content = content;
        }

        @Override
        public String getContent() throws UncheckedIOException {
            return content;
        }

        @Override
        public URI getURI() {
            return null;
        }
    }
}
