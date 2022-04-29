package com.chutneytesting.glacio.api;

import static com.chutneytesting.glacio.domain.parser.ParsingContext.PARSING_CONTEXT_KEYS.ENVIRONMENT;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.chutneytesting.glacio.domain.parser.StepFactory;
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

    public List<StepDefinitionDto> toChutneyStepDefinition(String text) {
        return toChutneyStepDefinition(text, DEFAULT_ENV);
    }

    public List<StepDefinitionDto> toChutneyStepDefinition(String text, String environment) {
        Feature feature = this.toGlacioModel(text);
        String featureName = feature.getName();
        Locale language = new Locale(feature.getLanguage().getCode());

        List<Example> examples = feature.getExamples();
        ParsingContext context = new ParsingContext();
        context.values.put(ENVIRONMENT, ofNullable(environment).orElse(DEFAULT_ENV));
        return examples.stream()
            .map(scenario -> toStepDefinitionDto(
                featureName,
                language,
                context,
                scenario)
            )
            .collect(Collectors.toList());
    }

    private StepDefinitionDto toStepDefinitionDto(String featureName, Locale lang, ParsingContext context, Example example) {
        String name = featureName + " - " + example.getName();

        List<StepDefinitionDto> scenarioSteps = example.getSteps().stream()
            .map(step -> this.stepFactory.toStepDefinition(lang, context, step))
            .map(StepDefinitionMapper::toStepDefinitionDto)
            .collect(Collectors.toList());

        return buildRootStep(scenarioSteps, context, name);
    }

    private StepDefinitionDto buildRootStep(List<StepDefinitionDto> subSteps, ParsingContext context, String text) {
        return new StepDefinitionDto(text, null, "", null, emptyMap(), subSteps, emptyMap(), emptyMap());
    }

    private Feature toGlacioModel(String text) {
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
