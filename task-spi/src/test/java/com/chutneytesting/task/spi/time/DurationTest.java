package com.chutneytesting.task.spi.time;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.function.Supplier;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DurationTest {

    @ParameterizedTest(name = "\"{0} is parsed to {1} ms\"")
    @MethodSource("parametersForParsing_nominal_cases")
    public void parsing_nominal_cases(String durationAsString, long expectedMilliseconds, String expectedStringRepresentation) {
        Duration duration = Duration.parse(durationAsString);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Math.abs(duration.toMilliseconds() - expectedMilliseconds)).isLessThan(10 * 60 * 1000); // comparison with +-1.5 sec precision for slow runner
        softly.assertThat(duration.toString()).isEqualTo(expectedStringRepresentation != null ? expectedStringRepresentation : duration.toMilliseconds() + " ms");
        softly.assertAll();
    }

    public static Object[] parametersForParsing_nominal_cases() {
        Supplier<Long> durationInMsUntil = () -> UntilHourDurationParserTest.millisTo(17, 42);
        return new Object[][]{
            {"10 m", 10 * 60 * 1000, "10 min"},
            {"until 17:42", durationInMsUntil.get(), null}
        };
    }

    @ParameterizedTest(name = "Parsing {0} fails")
    @MethodSource("parametersForParsing_error_cases")
    public void parsing_error_cases(String duration, String expectedErrorMessage) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> Duration.parse(duration))
            .withMessage("Cannot parse duration: " + duration + "\n" +
                "Available patterns are:\n" +
            "- Duration with unit: <positive number> (ms|sec|s|min|m|hours|hour|h|hour(s)|day(s)|d|days|day)\n" +
            "Samples:\n" +
                "\t 3 min\n" +
                "\t 4,5 hours\n" +
                "- Until hour: until <1..24>:<1..60>\n" +
                "Samples:\n" +
                "\t until 02:00\n" +
                "\t until 17:45");
    }

    public static Object[] parametersForParsing_error_cases() {
        return new Object[][]{
            {"-1.5 sec", "Duration field must have a positive value"},
            {"none", "Duration field must have a positive value"},
            {"NaN", "Duration field must have a positive value"},
            {"10 whatever", "Unknown time unit whatever"},
        };
    }
}
