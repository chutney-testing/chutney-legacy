package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Optional.ofNullable;

import com.github.fridujo.glacio.ast.Step;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlacioSimpleParser implements GlacioExecutableStepParser {

    private final static Pattern STEP_TEXT_PATTERN = Pattern.compile("^(?<task>\\(.*\\) )?(?<text>.*)$");
    private final static Predicate<String> STEP_TEXT_PREDICATE = STEP_TEXT_PATTERN.asPredicate();

    @Override
    public Integer priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean couldParse(String stepText) {
        return STEP_TEXT_PREDICATE.test(stepText);
    }

    @Override
    public String parseTaskType(Step step) {
        Matcher matcher = STEP_TEXT_PATTERN.matcher(step.getText());
        if (matcher.matches()) {
            String stepText = ofNullable(matcher.group("text")).orElse("");
            return ofNullable(matcher.group("task"))
                .map(this::extractTaskId)
                .orElse(stepText);
        }
        throw new IllegalArgumentException("Cannot parse task type from step text : "+step.getText());
    }

    private String extractTaskId(String taskGroup) {
        String withoutFirstChar = taskGroup.substring(1);
        return withoutFirstChar.substring(0, withoutFirstChar.length() - 2);
    }
}
