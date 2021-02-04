package com.chutneytesting.glacio;

import static com.chutneytesting.glacio.util.GherkinLanguageFileReader.createLanguagesKeywords;
import static com.chutneytesting.glacio.util.ParserClasspathReader.createGlacioParsers;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.environment.EnvironmentConfiguration;
import com.chutneytesting.glacio.api.GlacioAdapter;
import com.chutneytesting.glacio.domain.parser.IParseExecutableStep;
import com.chutneytesting.glacio.domain.parser.StepFactory;
import com.chutneytesting.glacio.domain.parser.StepFactory.EXECUTABLE_KEYWORD;
import com.chutneytesting.glacio.domain.parser.executable.DefaultGlacioParser;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class GlacioAdapterConfiguration {

    private final ExecutionConfiguration executionConfiguration;
    private final EnvironmentConfiguration environmentConfiguration;

    private final GlacioAdapter glacioAdapter;

    public GlacioAdapterConfiguration(String envFolderPath) throws IOException {
        this.executionConfiguration = new ExecutionConfiguration();
        this.environmentConfiguration = new EnvironmentConfiguration(envFolderPath);

        final StepFactory stepFactory = createExecutableStepFactory();
        this.glacioAdapter = new GlacioAdapter(stepFactory);
    }

    public GlacioAdapter glacioAdapter() {
        return glacioAdapter;
    }

    public ExecutionConfiguration executionConfiguration() {
        return executionConfiguration;
    }

    private StepFactory createExecutableStepFactory() throws IOException {
        Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> languagesKeywords = createLanguagesKeywords(EXECUTABLE_KEYWORD.class, "META-INF/extension/chutney.glacio-languages.json");
        Map<Pair<Locale, String>, IParseExecutableStep> glacioParsers = createGlacioParsers("META-INF/extension/chutney.glacio.parsers");
        DefaultGlacioParser defaultGlacioParser = new DefaultGlacioParser(executionConfiguration.taskTemplateRegistry(), environmentConfiguration.getEnvironmentEmbeddedApplication());
        return new StepFactory(
            languagesKeywords,
            glacioParsers,
            defaultGlacioParser
        );
    }

}
