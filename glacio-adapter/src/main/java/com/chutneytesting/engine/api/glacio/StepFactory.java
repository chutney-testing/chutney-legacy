package com.chutneytesting.engine.api.glacio;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.glacio.parse.IParseBusinessStep;
import com.chutneytesting.engine.api.glacio.parse.IParseExecutableStep;
import com.chutneytesting.engine.api.glacio.parse.IParseStrategy;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.model.Step;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class StepFactory {

    private final static String EXECUTABLE_STEP_TEXT_PATTERN_STRING = "^(?:%s)(?:\\W?\\s+)(?<sentence>(?<action>[a-zA-Z\\-_0-9]+)(\\s+.*)?)$";
    private Map<Locale, Pair<Pattern, Predicate<String>>> executableStepTextPatternsCache = new ConcurrentHashMap<>();

    private final Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords;
    private final Map<Pair<Locale, String>, IParseExecutableStep> glacioExecutableStepParsersLanguages;
    private final IParseExecutableStep defaultExecutableStepParser;
    private final IParseBusinessStep defaultBusinessStepParser;
    private final IParseStrategy defaultStrategyParser;

    public StepFactory(Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords,
                       Map<Pair<Locale, String>, IParseExecutableStep> glacioExecutableStepParsersLanguages,
                       IParseExecutableStep defaultExecutableStepParser,
                       IParseBusinessStep defaultBusinessStepParser,
                       IParseStrategy strategyParser) {
        this.executableStepLanguagesKeywords = executableStepLanguagesKeywords;
        this.glacioExecutableStepParsersLanguages = glacioExecutableStepParsersLanguages;
        this.defaultExecutableStepParser = defaultExecutableStepParser;
        this.defaultBusinessStepParser = defaultBusinessStepParser;
        this.defaultStrategyParser = strategyParser;
    }

    public enum EXECUTABLE_KEYWORD {DO}

    public boolean isExecutableStep(Locale lang, Step step) {
        Pair<Pattern, Predicate<String>> pattern = ofNullable(executableStepTextPatternsCache.get(lang))
            .orElseGet(() -> compileAndCachePattern(lang));
        return pattern.getRight().test(step.getText().trim());
    }

    public StepDefinition toStepDefinition(Locale lang, String environment, Step step) {
        Pair<Step, StepStrategyDefinition> result = buildStrategyDefinition(lang, step);

        Step stepWithoutStrategy = result.getLeft();
        StepStrategyDefinition stepStrategyDefinition = result.getRight();

        if (this.isExecutableStep(lang, step)) {
            return this.buildExecutableStep(lang, environment, stepWithoutStrategy, stepStrategyDefinition);
        } else {
            return buildBusinessLevelStep(lang, environment, stepWithoutStrategy, stepStrategyDefinition);
        }
    }

    private Pair<Step, StepStrategyDefinition> buildStrategyDefinition(Locale lang, Step step) {
        Pair<Step, List<StepStrategyDefinition>> result = defaultStrategyParser.parseStepAndStripStrategy(lang, step);
        if (result.getRight().size() > 0) {
            return Pair.of(result.getLeft(), result.getRight().get(0));
        }

        return Pair.of(step, null);
    }

    public StepDefinition buildExecutableStep(Locale lang, String environment, Step step, StepStrategyDefinition stepStrategyDefinition) {
        Optional<Pair<Pattern, Predicate<String>>> pattern = ofNullable(executableStepTextPatternsCache.get(lang));
        if (pattern.isPresent()) {
            Matcher matcher = pattern.get().getLeft().matcher(step.getText().trim());
            if (matcher.matches()) {
                String action = ofNullable(matcher.group("action")).orElse("");
                String sentence = ofNullable(matcher.group("sentence")).orElse("");
                Step stepWithoutAction = this.removeActionFromStep(sentence, step);

                return delegateStepParsing(lang, action, environment, stepWithoutAction, stepStrategyDefinition);
            }
        }
        throw new IllegalArgumentException("Step cannot be qualified as executable : " + step);
    }

    private Step removeActionFromStep(String sentence, Step step) {
        return new Step(step.isBackground(), step.getKeyword(), sentence, step.getArgument(), step.getSubsteps());
    }

    private StepDefinition delegateStepParsing(Locale lang, String action, String environment, Step step, StepStrategyDefinition stepStrategyDefinition) {
        return Optional.ofNullable(glacioExecutableStepParsersLanguages.get(Pair.of(lang, action)))
            .orElse(defaultExecutableStepParser)
            .mapToStepDefinition(environment, step, stepStrategyDefinition);
    }

    private StepDefinition buildBusinessLevelStep(Locale lang, String environment, Step step, StepStrategyDefinition stepStrategyDefinition) {
        List<StepDefinition> subSteps = buildSubSteps(lang, environment, step);
        return this.defaultBusinessStepParser.mapToStepDefinition(environment, step, subSteps, stepStrategyDefinition);
    }

    private List<StepDefinition> buildSubSteps(Locale lang, String environment, Step step) {
        return step.getSubsteps().stream()
            .map(subStep -> toStepDefinition(lang, environment, subStep))
            .collect(Collectors.toList());
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
}
