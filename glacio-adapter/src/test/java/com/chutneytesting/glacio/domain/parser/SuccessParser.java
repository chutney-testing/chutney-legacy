package com.chutneytesting.glacio.domain.parser;

import com.github.fridujo.glacio.model.Step;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SuccessParser extends ExecutableGlacioStepParser {

    public SuccessParser() {
        super(null, null, null, null);
    }

    @Override
    public String parseActionType(Step step) {
        return "success";
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        Map<Locale, Set<String>> keywords = new HashMap<>();
        keywords.put(new Locale("tt", "TT"),
            new HashSet<>(Arrays.asList("SUCCESS", "SUCC")));
        return keywords;
    }
}
