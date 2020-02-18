package com.chutneytesting.task.spi.time;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DurationWithUnitParser implements DurationParser {

    private static final String SIMPLE_DURATION_REGEX = "^(?<value>\\d+(?:[.,]\\d+)?)\\s+(?<unit>" + DurationUnit.regex() + ")$";

    private static final Pattern SIMPLE_DURATION_PATTERN = Pattern.compile(SIMPLE_DURATION_REGEX);

    @Override
    public Optional<Duration> parse(String literalDuration) {
        Matcher matcher = SIMPLE_DURATION_PATTERN.matcher(literalDuration.trim().toLowerCase());
        final Optional<Duration> parsingResult;
        if (matcher.matches()) {
            double value = Double.parseDouble(matcher.group("value").replace(',', '.'));
            DurationUnit unit = DurationUnit.parse(matcher.group("unit"));
            parsingResult = Optional.of(new Duration(value, unit));
        } else {
            parsingResult = Optional.empty();
        }

        return parsingResult;
    }

    @Override
    public String description() {
        return "Duration with unit: <positive number> " + DurationUnit.regex() + "\n" +
            "Samples:\n" +
            "\t 3 min\n" +
            "\t 4,5 hours";
    }
}
