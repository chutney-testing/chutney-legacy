package com.chutneytesting.engine.api.glacio;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.model.Example;
import com.github.fridujo.glacio.model.Feature;
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

    private final StepFactory stepFactory;

    public GlacioAdapter(StepFactory stepFactory) {
        this.stepFactory = stepFactory;
    }

    public List<StepDefinition> toChutneyStepDefinition(String text) {
        return toChutneyStepDefinition(text, DEFAULT_ENV);
    }

    public List<StepDefinition> toChutneyStepDefinition(String text, String environment) {
        Feature feature = this.toFeature(text);
        List<Example> examples = feature.getExamples();

        return examples.stream()
            .map(scenario -> toStepDefinition(
                feature.getName(),
                new Locale(feature.getLanguage().getCode()),
                ofNullable(environment).orElse(DEFAULT_ENV),
                scenario)
            )
            .collect(Collectors.toList());
    }

    private StepDefinition toStepDefinition(String featureName, Locale lang, String environment, Example example) {
        String name = featureName + " - " + example.getName();

        List<StepDefinition> scenarioSteps = example.getSteps().stream()
            .map(step -> this.stepFactory.toStepDefinition(lang, environment, step))
            .collect(Collectors.toList());

        return buildRootStep(scenarioSteps, environment, name);
    }

    private StepDefinition buildRootStep(List<StepDefinition> subSteps, String environment, String text) {
        return new StepDefinition(text, null, "", null, emptyMap(), subSteps, emptyMap(), environment);
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
