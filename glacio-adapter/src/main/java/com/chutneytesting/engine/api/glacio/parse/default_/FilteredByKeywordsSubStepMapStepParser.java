package com.chutneytesting.engine.api.glacio.parse.default_;

import static com.chutneytesting.engine.api.glacio.parse.default_.ParsingTools.arrayToOrPattern;
import static com.chutneytesting.engine.api.glacio.parse.default_.ParsingTools.removeKeyword;

import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.github.fridujo.glacio.ast.Step;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilteredByKeywordsSubStepMapStepParser implements StepParser<Map<String, Object>> {

    private final StepParser<Entry<String, Object>> entryStepParser;
    private final Pattern startWithPattern;
    private final Predicate<String> startWithPredicate;

    public FilteredByKeywordsSubStepMapStepParser(StepParser<Entry<String, Object>> entryStepParser, String... startingWords) {
        this.startWithPattern = Pattern.compile("^(?<keyword>" + arrayToOrPattern(startingWords) + ")(?: .*)$");
        this.startWithPredicate = startWithPattern.asPredicate();
        this.entryStepParser = entryStepParser;
    }

    @Override
    public Map<String, Object> parseStep(Step step) {
        return step.getSubsteps().stream()
            .filter(substep -> startWithPredicate.test(substep.getText()))
            .map(s -> removeKeyword(startWithPattern, s))
            .map(entryStepParser::parseStep)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

}
