package com.chutneytesting.glacio.domain.parser;

import static java.util.Arrays.asList;

import com.github.fridujo.glacio.model.Step;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AnotherDebugParser extends ExecutableGlacioStepParser {

    public AnotherDebugParser() {
        super(null, null, null);
    }

    @Override
    public String parseTaskType(Step step) {
        return "debug";
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        Map<Locale, Set<String>> keywords = new HashMap<>();
        keywords.put(new Locale("tt", "TT"),
            new HashSet<>(asList("DEBUG", "DBG")));
        return keywords;
    }
}
