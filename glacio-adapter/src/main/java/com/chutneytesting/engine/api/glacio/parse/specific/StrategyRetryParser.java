package com.chutneytesting.engine.api.glacio.parse.specific;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.glacio.parse.default_.StrategyParser;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import com.chutneytesting.task.spi.time.DurationUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrategyRetryParser extends StrategyParser {

    private final String RETRY_PARAMETERS_PATTERN_STRING = "(?<timeout>every\\s+(?<tval>.*))(?<delay>(for|until)\\s+(?<dval>.*))";
    private final Pattern pattern = Pattern.compile(RETRY_PARAMETERS_PATTERN_STRING);

    @Override
    public Map<Locale, Set<String>> keywords() {
        Map<Locale, Set<String>> keywords = new HashMap<>();
        keywords.put(Locale.ENGLISH,
            new HashSet<>(Arrays.asList("retry", "retry-with-timeout", "retry_with_timeout")));
        return keywords;
    }

    @Override
    public StepStrategyDefinition toStrategyDef(Locale lang, String parameters) {
        return new StepStrategyDefinition("retry-with-timeout", parseProperties(lang, parameters));
    }

    @Override
    public StrategyProperties parseProperties(Locale lang, String parameters) {
        Map<String, Object> params = new HashMap<>(2);

        Matcher matcher = pattern.matcher(parameters.trim().replaceAll("\\s+", " "));

        while(matcher.find()) {
            ofNullable(matcher.group("timeout")).ifPresent( t -> params.put("timeOut", DurationParser.parseDurationValue(matcher.group("tval"))) );
            ofNullable(matcher.group("delay")).ifPresent( t ->  params.put("retryDelay", DurationParser.parseDurationValue(matcher.group("dval"))) );
        }

        return new StrategyProperties(params);
    }

    static class DurationParser {
        private static final String SIMPLE_DURATION_REGEX = "(?<param>(?<value>\\d+(?:[.,]\\d+)?)\\s+(?<unit>" + DurationUnit.regex() + "))";
        private static final Pattern SIMPLE_DURATION_PATTERN = Pattern.compile(SIMPLE_DURATION_REGEX);

        private static String parseDurationValue(String literalDuration) {
            Matcher matcher = SIMPLE_DURATION_PATTERN.matcher(literalDuration.trim().toLowerCase());
            if (matcher.find()) {
                return matcher.group("param");
            }

            return "";
        }
    }

}
