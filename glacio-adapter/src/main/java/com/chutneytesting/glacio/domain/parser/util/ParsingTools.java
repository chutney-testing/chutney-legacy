package com.chutneytesting.glacio.domain.parser.util;

import com.github.fridujo.glacio.model.Step;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingTools {

    private ParsingTools() {}

    public static Step removeKeyword(Pattern pattern, Step step) {
        final Matcher matcher = pattern.matcher(step.getText());
        if (matcher.matches()) {
            final String keyword = matcher.group("keyword");
            return new Step(step.isBackground(), step.getKeyword(), step.getText().substring(keyword.length()).trim(), step.getArgument(), step.getSubsteps());
        }
        throw new IllegalStateException();
    }

    public static String arrayToOrPattern(String... startingWords) {
        return Arrays.stream(startingWords).reduce((s, s2) -> s + "|" + s2).orElse("");
    }
}
