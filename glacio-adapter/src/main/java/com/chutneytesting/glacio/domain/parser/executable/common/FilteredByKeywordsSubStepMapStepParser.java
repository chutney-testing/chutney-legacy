package com.chutneytesting.glacio.domain.parser.executable.common;

import com.chutneytesting.glacio.domain.parser.GlacioStepParser;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.chutneytesting.glacio.domain.parser.util.ParsingTools;
import com.github.fridujo.glacio.model.Step;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilteredByKeywordsSubStepMapStepParser implements GlacioStepParser<Map<String, Object>> {

    private final GlacioStepParser<Entry<String, Object>> entryStepParser;
    private final Pattern startWithPattern;
    private final Predicate<String> startWithPredicate;

    public FilteredByKeywordsSubStepMapStepParser(GlacioStepParser<Entry<String, Object>> entryStepParser, String... startingWords) {
        this.startWithPattern = Pattern.compile("^(?<keyword>" + ParsingTools.arrayToOrPattern(startingWords) + ")(?: .*)$");
        this.startWithPredicate = startWithPattern.asPredicate();
        this.entryStepParser = entryStepParser;
    }

    @Override
    public Map<String, Object> parseGlacioStep(ParsingContext context, Step step) {
        return step.getSubsteps().stream()
            .filter(substep -> startWithPredicate.test(substep.getText()))
            .map(s -> ParsingTools.removeKeyword(startWithPattern, s))
            .map(glacioStep -> entryStepParser.parseGlacioStep(context, glacioStep))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

}
