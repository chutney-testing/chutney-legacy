package com.chutneytesting.engine.api.glacio;

import static com.chutneytesting.tools.Streams.identity;
import static java.util.Optional.empty;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.glacio.StepFactory.EXECUTABLE_KEYWORD;
import com.chutneytesting.engine.api.glacio.parse.GlacioBusinessStepParser;
import com.chutneytesting.engine.api.glacio.parse.IParseExecutableStep;
import com.chutneytesting.engine.api.glacio.parse.IParseStrategy;
import com.chutneytesting.engine.api.glacio.parse.default_.DefaultGlacioParser;
import com.chutneytesting.engine.api.glacio.parse.default_.StrategyParser;
import com.chutneytesting.engine.api.glacio.parse.specific.strategy.StrategyRetryParser;
import com.chutneytesting.engine.api.glacio.parse.specific.strategy.StrategySoftAssertParser;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.infra.JsonFilesEnvironmentRepository;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlacioAdapterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlacioAdapterConfiguration.class);

    private final ExecutionConfiguration executionConfiguration;

    private final GlacioAdapter glacioAdapter;
    private final Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords;
    private final List<IParseExecutableStep> glacioExecutableStepParsers;
    private final Map<Pair<Locale, String>, IParseExecutableStep> glacioExecutableStepParsersLanguages;

    private final StepFactory stepFactory;
    private final IParseStrategy strategyParser;

    private final EnvironmentRepository environmentRepository;
    private final EnvironmentService environmentService;

    public GlacioAdapterConfiguration() throws IOException {
        this(new ExecutionConfiguration(), "conf", "conf/endpoints.json");
    }

    public GlacioAdapterConfiguration(Long engineReporterTTL, String environmentFolderPath, String agentNetworkFilePath) throws IOException {
        this(new ExecutionConfiguration(engineReporterTTL), environmentFolderPath, agentNetworkFilePath);
    }

    public GlacioAdapterConfiguration(ExecutionConfiguration executionConfiguration, String environmentFolderPath, String agentNetworkFilePath) throws IOException {
        this.executionConfiguration = executionConfiguration;

        glacioExecutableStepParsers = createGlacioExecutableStepParsers();
        glacioExecutableStepParsersLanguages = createGlacioExecutableStepParsersLanguages();
        executableStepLanguagesKeywords = createExecutableStepLanguagesKeywords();

        environmentRepository = createEnvironmentRepository(environmentFolderPath);
        environmentService = createEnvironmentService();

        strategyParser = createStrategyDefinitionFactory();
        stepFactory = createExecutableStepFactory();
        glacioAdapter = createGlacioAdapter();
    }

    public ExecutionConfiguration executionConfiguration() {
        return executionConfiguration;
    }

    public GlacioAdapter glacioAdapter() {
        return glacioAdapter;
    }

    public EnvironmentService environmentService() {
        return environmentService;
    }

    public List<IParseExecutableStep> glacioExecutableStepParsers() {
        return glacioExecutableStepParsers;
    }

    public Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords() {
        return executableStepLanguagesKeywords;
    }

    public Map<Pair<Locale, String>, IParseExecutableStep> glacioExecutableStepParsersLanguages() {
        return glacioExecutableStepParsersLanguages;
    }

    private Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> createExecutableStepLanguagesKeywords() throws IOException {
        return GherkinLanguageFileReader.readAsMapLocale(EXECUTABLE_KEYWORD.class, GlacioAdapterConfiguration.class
            .getClassLoader().getResources("META-INF/extension/chutney.glacio-languages.json"));
    }

    private List<IParseExecutableStep> createGlacioExecutableStepParsers() {
        return ExtensionLoaders
            .classpathToClass("META-INF/extension/chutney.glacio.parsers")
            .load()
            .stream()
            .map(ThrowingFunction.toUnchecked(this::instantiateGlacioParser))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(identity(c -> LOGGER.info("Loading glacio parser : {}", c.getClass().getSimpleName())))
            .collect(Collectors.toList());
    }

    private Map<Pair<Locale, String>, IParseExecutableStep> createGlacioExecutableStepParsersLanguages() {
        Optional<Map<Pair<Locale, String>, IParseExecutableStep>> result = glacioExecutableStepParsers.stream()
            .map(this::parserPairKeywords)
            .reduce(this::mergeParserPairKeywords);
        return result.orElseGet(HashMap::new);
    }

    private EnvironmentRepository createEnvironmentRepository(String storeFolderPath) {
        return new JsonFilesEnvironmentRepository(storeFolderPath);
    }

    private EnvironmentService createEnvironmentService() {
        return new EnvironmentService(environmentRepository);
    }

    private IParseStrategy createStrategyDefinitionFactory() {
        return new StrategyParser(Arrays.asList(new StrategySoftAssertParser(), new StrategyRetryParser())); // TODO : Load from FILE
    }

    private StepFactory createExecutableStepFactory() {
        return new StepFactory(
            executableStepLanguagesKeywords,
            glacioExecutableStepParsersLanguages,
            new DefaultGlacioParser(executionConfiguration.taskTemplateRegistry(),
                environmentService),
            new GlacioBusinessStepParser(),
            strategyParser
        );
    }

    private GlacioAdapter createGlacioAdapter() {
        return new GlacioAdapter(stepFactory);
    }

    private Optional<IParseExecutableStep> instantiateGlacioParser(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        if (!IParseExecutableStep.class.isAssignableFrom(clazz)) {
            LOGGER.warn("{} is declared as glacio parser but does not implement GlacioExecutableStepParser interface. Ignore it.", clazz.getSimpleName());
            return empty();
        }
        Class<IParseExecutableStep> parserClazz = (Class<IParseExecutableStep>) clazz;
        return Optional.of(parserClazz.newInstance());
    }

    private Map<Pair<Locale, String>, IParseExecutableStep> parserPairKeywords(IParseExecutableStep parser) {
        return parser.keywords().entrySet().stream()
            .flatMap(e -> e.getValue().stream().map(v -> Pair.of(e.getKey(), v)))
            .collect(Collectors.toMap(o -> o, o -> parser));
    }

    private Map<Pair<Locale, String>, IParseExecutableStep> mergeParserPairKeywords(Map<Pair<Locale, String>, IParseExecutableStep> o1,
                                                                                    Map<Pair<Locale, String>, IParseExecutableStep> o2) {
        o2.forEach((k, v) -> o1.merge(k, v, (p1, p2) -> {
            LOGGER.warn("Same pair {} declared for parsers {} and {}. Take the first one.", k, p1.getClass().getSimpleName(), p2.getClass().getSimpleName());
            return p1;
        }));
        return o1;
    }
}
