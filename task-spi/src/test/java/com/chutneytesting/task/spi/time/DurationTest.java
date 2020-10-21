package com.chutneytesting.task.spi.time;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.function.Supplier;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class DurationTest {

    @Test
    @Parameters
    @TestCaseName("{0} is parsed to {1} ms")
    public void parsing_nominal_cases(String durationAsString, double expectedMilliseconds, String expectedStringRepresentation) {
        Duration duration = Duration.parse(durationAsString);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Math.abs(duration.toMilliseconds() - expectedMilliseconds)).isLessThan(10 * 60 * 1000); // comparison with +-1.5 sec precision for slow runner
        softly.assertThat(duration.toString()).isEqualTo(expectedStringRepresentation != null ? expectedStringRepresentation : duration.toMilliseconds() + " ms");
        softly.assertAll();
    }

    public static Object[] parametersForParsing_nominal_cases() {
        Supplier<Long> durationInMsUntil = () -> UntilHourDurationParserTest.millisTo(17, 42);
        return new Object[][]{
            {"5890 ns", 5890 * 0.00001, "5890 ns"},
            {"765 \u03bcs", 765 * 0.001, "765 \u03bcs"},
            {"10 m", 10 * 60 * 1000, "10 min"},
            {"until 17:42", durationInMsUntil.get(), null}
        };
    }

    @Test
    @Parameters
    @TestCaseName("Parsing {0} fails")
    public void parsing_error_cases(String duration, String expectedErrorMessage) throws Exception {
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
