package com.chutneytesting.engine.api.glacio.parse.specific;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.chutneytesting.engine.api.glacio.parse.default_.EmptyParser;
import com.github.fridujo.glacio.model.Step;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlacioSleepParser extends GlacioExecutableStepParser {

    private final static Pattern STEP_TEXT_PATTERN = Pattern.compile("^(?:[Ss]leep|[Aa]?[Ww]ait|[Rr]est|[Pp]ause) (?:(for|during) )?(?<duration>.*)$");

    public GlacioSleepParser() {
        super(EmptyParser.noTargetParser,
            GlacioSleepParser::parseTaskInputs,
            EmptyParser.emptyMapParser);
    }

    @Override
    public String parseTaskType(Step step) {
        return "sleep";
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        Map<Locale, Set<String>> keywords = new HashMap<>();
        keywords.put(Locale.ENGLISH,
            new HashSet<>(Arrays.asList("Sleep", "sleep", "Await", "await", "Wait", "wait", "Rest", "rest", "Pause", "pause")));
        return keywords;
    }

    private static Map<String, Object> parseTaskInputs(Step step) {
        Matcher matcher = STEP_TEXT_PATTERN.matcher(step.getText());
        if (matcher.matches()) {
            String duration = ofNullable(matcher.group("duration"))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find duration input in step :" + step));
            return Collections.singletonMap("duration", duration);
        }
        throw new IllegalArgumentException("Cannot match defined pattern : " + STEP_TEXT_PATTERN);
    }
}
