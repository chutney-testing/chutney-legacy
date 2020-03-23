package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Optional.ofNullable;

import com.github.fridujo.glacio.ast.Step;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlacioSleepParser extends GlacioParser {

    private final static Pattern STEP_TEXT_PATTERN = Pattern.compile("^(?:sleep|a?wait|stop|rest|(?:stand by)) (?:(for|during) )?(?<duration>.*)$");
    private final static Predicate<String> STEP_TEXT_PREDICATE = STEP_TEXT_PATTERN.asPredicate();

    @Override
    public Integer priority() {
        return 2000000000;
    }

    @Override
    public boolean couldParse(Step step) {
        return STEP_TEXT_PREDICATE.test(step.getText());
    }

    @Override
    public Map<String, Object> parseTaskInputs(Step step) {
        Matcher matcher = STEP_TEXT_PATTERN.matcher(step.getText());
        if (matcher.matches()) {
            String duration = ofNullable(matcher.group("duration"))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find duration input in step :" + step));
            return Collections.singletonMap("duration", duration);
        }
        throw new IllegalArgumentException("Cannot match defined pattern : " + STEP_TEXT_PATTERN);
    }

    @Override
    public String parseTaskType(Step step) {
        return "sleep";
    }
}
