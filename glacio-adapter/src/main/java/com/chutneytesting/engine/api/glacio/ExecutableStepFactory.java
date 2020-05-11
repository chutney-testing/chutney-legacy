package com.chutneytesting.engine.api.glacio;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.ast.Step;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;

public class ExecutableStepFactory {

    private final static String EXECUTABLE_STEP_TEXT_PATTERN_STRING = "^(?<trigger>%s)(\\W+)(?<sentence>(?<action>\\w+)(\\s+.*)?)$";
    private Map<Locale, Pair<Pattern, Predicate<String>>> executableStepTextPatternsCache = new ConcurrentHashMap<>();

    private final Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords;
    private final Map<Pair<Locale, String>, GlacioExecutableStepParser> glacioExecutableStepParsersLanguages;
    private final GlacioExecutableStepParser defaultGlacioParser;

    public ExecutableStepFactory(Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords,
                                 Map<Pair<Locale, String>, GlacioExecutableStepParser> glacioExecutableStepParsersLanguages,
                                 GlacioExecutableStepParser defaultGlacioParser) {
        this.executableStepLanguagesKeywords = executableStepLanguagesKeywords;
        this.glacioExecutableStepParsersLanguages = glacioExecutableStepParsersLanguages;
        this.defaultGlacioParser = defaultGlacioParser;
    }

    public StepDefinition build(Locale lang, String environment, Step step) {
        Optional<Pair<Pattern, Predicate<String>>> pattern = ofNullable(executableStepTextPatternsCache.get(lang));
        if (pattern.isPresent()) {
            Matcher matcher = pattern.get().getLeft().matcher(step.getText());
            if (matcher.matches()) {
                String action = ofNullable(matcher.group("action")).orElse("");
                String sentence = ofNullable(matcher.group("sentence")).orElse("");
                return delegateStepParsing(Pair.of(lang, action), environment, rebuildStepUsing(sentence, step));
            }
        }
        throw new IllegalArgumentException("Step cannot be qualified as executable : " + step);
    }

    private StepDefinition delegateStepParsing(Pair<Locale, String> parserKeyword, String environment, Step step) {
        return Optional.ofNullable(glacioExecutableStepParsersLanguages.get(parserKeyword))
            .orElse(defaultGlacioParser)
            .mapToStepDefinition(environment, step);
    }

    public boolean isExecutableStep(Locale lang, Step step) {
        Pair<Pattern, Predicate<String>> pattern = ofNullable(executableStepTextPatternsCache.get(lang))
            .orElseGet(() -> compileAndCachePattern(lang));
        return pattern.getRight().test(step.getText());
    }

    private Step rebuildStepUsing(String sentence, Step step) {
        return new Step(step.getPosition(), sentence, step.getSubsteps(), step.getDocString(), step.getDataTable());
    }

    private Pair<Pattern, Predicate<String>> compileAndCachePattern(Locale lang) {
        Map<EXECUTABLE_KEYWORD, Set<String>> executable_keywordSetMap = ofNullable(executableStepLanguagesKeywords.get(lang))
            .orElseThrow(() -> new IllegalArgumentException("Language " + lang + " not supported. Define it in configuration file."));

        executable_keywordSetMap.values().stream()
            .flatMap(Collection::stream)
            .reduce((k1, k2) -> k1 + "|" + k2)
            .map(p -> Pattern.compile(String.format(EXECUTABLE_STEP_TEXT_PATTERN_STRING, p)))
            .ifPresent(p -> executableStepTextPatternsCache.put(lang, Pair.of(p, p.asPredicate())));

        return executableStepTextPatternsCache.get(lang);
    }

    public enum EXECUTABLE_KEYWORD {DO}
}
