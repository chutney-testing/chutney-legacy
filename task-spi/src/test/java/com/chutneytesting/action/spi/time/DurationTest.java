package com.chutneytesting.action.spi.time;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DurationTest {

    @ParameterizedTest(name = "\"{0} is parsed to {1} ms\"")
    @MethodSource("parametersForParsing_nominal_cases")
    public void parsing_nominal_cases(String durationAsString, Double expectedMilliseconds, String expectedStringRepresentation) {
        Duration duration = Duration.parse(durationAsString);
        SoftAssertions softly = new SoftAssertions();

        if (Double.MIN_VALUE == expectedMilliseconds) {
            softly.assertThat(duration.toMilliseconds()).isPositive();
        } else {
            softly.assertThat(Math.abs(duration.toMilliseconds() - expectedMilliseconds)).isZero();
        }

        if (expectedStringRepresentation.isBlank()) {
            softly.assertThat(duration.toString()).isNotBlank();
        } else {
            softly.assertThat(duration.toString()).isEqualTo(expectedStringRepresentation);
        }
        softly.assertAll();
    }

    public static Object[] parametersForParsing_nominal_cases() {
        return new Object[][]{
            {"5890 ns", 0.0, "5890 ns"},
            {"765 \u03bcs", 1.0, "765 \u03bcs"},
            {"10 m", 10 * 60 * 1000.0, "10 min"},
            {"until 17:42", Double.MIN_VALUE, ""}
        };
    }

    @ParameterizedTest(name = "Parsing {0} fails")
    @MethodSource("parametersForParsing_error_cases")
    public void parsing_error_cases(String duration, String expectedErrorMessage) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> Duration.parse(duration))
            .withMessage("Cannot parse duration: " + duration + "\n" +
                "Available patterns are:\n" +
            "- Duration with unit: <positive number> (ns|\u03bcs|\u00b5s|ms|sec|s|min|m|hours|hour|h|hour(s)|day(s)|d|days|day)\n" +
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
