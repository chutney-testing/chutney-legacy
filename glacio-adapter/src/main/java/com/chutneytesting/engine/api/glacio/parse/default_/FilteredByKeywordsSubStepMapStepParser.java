package com.chutneytesting.engine.api.glacio.parse.default_;

import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.github.fridujo.glacio.ast.Step;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilteredByKeywordsSubStepMapStepParser implements StepParser<Map<String, Object>> {

    private StepParser<Entry<String, Object>> entryStepParser;
    private Pattern startWithPattern;
    private Predicate<String> startWithPredicate;

    public FilteredByKeywordsSubStepMapStepParser(StepParser<Entry<String, Object>> entryStepParser, String... startingWords) {
        this.startWithPattern = Pattern.compile("^(?<keyword>" + arrayToOrPattern(startingWords) + ")(?: .*)$");
        this.startWithPredicate = startWithPattern.asPredicate();
        this.entryStepParser = entryStepParser;
    }

    @Override
    public Map<String, Object> parseStep(Step step) {
        return step.getSubsteps().stream()
            .filter(substep -> startWithPredicate.test(substep.getText()))
            .map(this::cleanStepText)
            .map(entryStepParser::parseStep)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private String arrayToOrPattern(String... startingWords) {
        return Arrays.stream(startingWords).reduce((s, s2) -> s + "|" + s2).orElse("");
    }

    private Step cleanStepText(Step step) {
        Matcher matcher = startWithPattern.matcher(step.getText());
        if (matcher.matches()) {
            String keyword = matcher.group("keyword");
            return new Step(step.getPosition(), step.getText().substring(keyword.length()).trim(), step.getSubsteps(), step.getDocString(), step.getDataTable());
        }
        throw new IllegalStateException();
    }
}
