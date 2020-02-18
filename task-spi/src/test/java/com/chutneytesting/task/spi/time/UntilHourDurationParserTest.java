package com.chutneytesting.task.spi.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class UntilHourDurationParserTest {

    private DurationParser untilHourDurationParser = new UntilHourDurationParser();

    @Test
    @Parameters
    @TestCaseName("{0} is parsed to {1} ms")
    public void parsing_nominal_cases(String literalDuration, long expectedMilliseconds) {
        Optional<Duration> optionalDuration = untilHourDurationParser.parse(literalDuration);
        assertThat(optionalDuration).as("Parsing result").isPresent();

        Duration duration = optionalDuration.get();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Math.abs(duration.toMilliseconds() - expectedMilliseconds)).isLessThan(10 * 60 * 1000); // comparison with +-10 min precision
        softly.assertThat(duration.toString()).isEqualTo(duration.toMilliseconds() + " ms");
        softly.assertAll();
    }

    public static Object[] parametersForParsing_nominal_cases() {
        return new Object[][]{
            {" until 02:00 ", millisTo(2, 0)},
            {"until   19:37 ", millisTo(19, 37)},
        };
    }

    @Test
    @Parameters
    @TestCaseName("{0} is not valid")
    public void unmatched_parsing(String literalDuration) {
        Optional<Duration> optionalDuration = untilHourDurationParser.parse(literalDuration);
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
        assertThat(untilHourDurationParser.description()).isEqualTo("Until hour: until <1..24>:<1..60>\n" +
            "Samples:\n" +
            "\t until 02:00\n" +
            "\t until 17:45");
    }

    static long millisTo(int hours, int minutes) {
        LocalDateTime targetTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).withHour(hours).withMinute(minutes);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1);
        }
        return java.time.Duration.between(now, targetTime).toMillis();
    }
}
