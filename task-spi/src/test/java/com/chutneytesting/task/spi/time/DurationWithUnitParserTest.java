package com.chutneytesting.task.spi.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class DurationWithUnitParserTest {

    private DurationParser durationWithUnitParser = new DurationWithUnitParser();

    @Test
    @Parameters
    @TestCaseName("{0} is parsed to {1} ms")
    public void parsing_nominal_cases(String durationAsString, long expectedMilliseconds, String expectedStringRepresentation) throws Exception {
        Optional<Duration> optionalDuration = durationWithUnitParser.parse(durationAsString);
        assertThat(optionalDuration).as("Parsing result").isPresent();

        Duration duration = optionalDuration.get();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(duration.toMilliseconds()).isEqualTo(expectedMilliseconds);
        softly.assertThat(duration.toString()).isEqualTo(expectedStringRepresentation);
        softly.assertAll();
    }

    public static Object[] parametersForParsing_nominal_cases() {
        return new Object[][]{
            {"2654 ns", 0, "2654 ns"},
            {"567898 ns", 6, "567898 ns"},
            {"198 \u00b5s", 0, "198 \u03bcs"},
            {"987 \u03bcs", 1, "987 \u03bcs"},
            {"15   ms", 15, "15 ms"},
            {"1.5 s", (long) (1.5 * 1000), "1.5 sec"},
            {"2 sec", 2 * 1000, "2 sec"},
            {"10 m", 10 * 60 * 1000, "10 min"},
            {"3.9 Min", (long) (3.9 * 60 * 1000), "3.9 min"},
            {"0,5 H", (long) (30 * 60 * 1000), "0.5 hour(s)"},
            {"2,2 Days", (long) ((48 + 4.8) * 60 * 60 * 1000), "2.2 day(s)"},
        };
    }

    @Test
    @Parameters
    @TestCaseName("{0} is not valid")
    public void unmatched_parsing(String literalDuration) {
        Optional<Duration> optionalDuration = durationWithUnitParser.parse(literalDuration);
        assertThat(optionalDuration).as("Parsing result").isEmpty();
    }

    public static Object[] parametersForUnmatched_parsing() {
        return new Object[][]{
            {"lol"},
            {"42"},
        };
    }

    @Test
    public void description_contains_regex_and_samples() {
        assertThat(durationWithUnitParser.description()).isEqualTo("Duration with unit: <positive number> (ns|\u03bcs|\u00b5s|ms|sec|s|min|m|hours|hour|h|hour(s)|day(s)|d|days|day)\n" +
            "Samples:\n" +
            "\t 3 min\n" +
            "\t 4,5 hours");
    }
}
