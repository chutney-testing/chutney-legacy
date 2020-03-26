package com.chutneytesting.engine.api.glacio;

import static com.chutneytesting.tools.Streams.identity;
import static java.util.Optional.empty;

import com.chutneytesting.engine.api.glacio.ExecutableStepFactory.EXECUTABLE_KEYWORD;
import com.chutneytesting.engine.api.glacio.parse.DefaultGlacioParser;
import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlacioAdapterSpringConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlacioAdapterSpringConfiguration.class);

    @Bean
    public Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords() throws IOException {
        Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords = new HashMap<>();
        for (URL url : Collections.list(GlacioAdapterSpringConfiguration.class
            .getClassLoader().getResources("META-INF/extension/chutney.glacio-languages.json"))) {

            try (InputStream is = url.openStream()) {
                JSONObject langObject = (JSONObject) JSONValue.parse(is);
                langObject.forEach((lang, keyWordObject) -> {
                    Locale localeKey = LocaleUtils.toLocale(lang);
                    Map<EXECUTABLE_KEYWORD, Set<String>> keywords =
                        Optional.ofNullable(executableStepLanguagesKeywords.get(localeKey)).orElseGet(HashMap::new);
                    ((JSONObject) keyWordObject).forEach((k, keyword) -> {
                        EXECUTABLE_KEYWORD execKeyword = EXECUTABLE_KEYWORD.valueOf(k.toUpperCase());
                        Set<String> keywordsSet = Optional.ofNullable(keywords.get(execKeyword)).orElseGet(HashSet::new);
                        keywordsSet.addAll((List<String>) keyword);
                        keywords.put(execKeyword, keywordsSet);
                    });
                    executableStepLanguagesKeywords.put(localeKey, keywords);
                });
            }
        }
        return executableStepLanguagesKeywords;
    }

    @Bean
    public List<GlacioExecutableStepParser> glacioExecutableStepParsers() {
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

    @Bean
    public Map<Pair<Locale, String>, GlacioExecutableStepParser> glacioExecutableStepParsersLanguages(List<GlacioExecutableStepParser> glacioExecutableStepParsers) {
        Optional<Map<Pair<Locale, String>, GlacioExecutableStepParser>> result = glacioExecutableStepParsers.stream()
            .map(this::parserPairKeywords)
            .reduce(this::mergeParserPairKeywords);
        return result.orElseGet(HashMap::new);
    }

    @Bean
    public ExecutableStepFactory executableStepFactory(Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords,
                                                       Map<Pair<Locale, String>, GlacioExecutableStepParser> glacioExecutableStepParsersLanguages,
                                                       TaskTemplateRegistry taskTemplateRegistry) {
        return new ExecutableStepFactory(executableStepLanguagesKeywords, glacioExecutableStepParsersLanguages, new DefaultGlacioParser(taskTemplateRegistry));
    }

    private Optional<GlacioExecutableStepParser> instantiateGlacioParser(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        if (!GlacioExecutableStepParser.class.isAssignableFrom(clazz)) {
            LOGGER.warn("{} is declared as glacio parser but does not implement GlacioExecutableStepParser interface. Ignore it.", clazz.getSimpleName());
            return empty();
        }
        Class<GlacioExecutableStepParser> parserClazz = (Class<GlacioExecutableStepParser>) clazz;
        return Optional.of(parserClazz.newInstance());
    }

    private Map<Pair<Locale, String>, GlacioExecutableStepParser> parserPairKeywords(GlacioExecutableStepParser parser) {
        return parser.keywords().entrySet().stream()
            .flatMap(e -> e.getValue().stream().map(v -> Pair.of(e.getKey(), v)))
            .collect(Collectors.toMap(o -> o, o -> parser));
    }

    private Map<Pair<Locale, String>, GlacioExecutableStepParser> mergeParserPairKeywords(Map<Pair<Locale, String>, GlacioExecutableStepParser> o1,
                                                                                          Map<Pair<Locale, String>, GlacioExecutableStepParser> o2) {
        o2.forEach((k, v) -> o1.merge(k, v, (p1, p2) -> {
            LOGGER.warn("Same pair {} declared for parsers {} and {}. Take the first one.", k, p1.getClass().getSimpleName(), p2.getClass().getSimpleName());
            return p1;
        }));
        return o1;
    }
}
