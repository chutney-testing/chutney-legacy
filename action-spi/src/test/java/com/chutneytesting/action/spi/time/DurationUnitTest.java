package com.chutneytesting.action.spi.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DurationUnitTest {


    public static Object[] data() {
        return new Object[][]{
            {"ns", DurationUnit.NANOS},
            {"\u00b5s", DurationUnit.MICROS},
            {"\u03bcs", DurationUnit.MICROS},
            {"   ms  ", DurationUnit.MILLIS},
            {"s", DurationUnit.SECONDS},
            {"SeC ", DurationUnit.SECONDS},
            {"m", DurationUnit.MINUTES},
            {"mIn", DurationUnit.MINUTES}
        };
    }

    @ParameterizedTest
    @MethodSource("data")
    public void should_parse_unit(String unit, DurationUnit expectedDuration) {
        DurationUnit durationUnit = DurationUnit.parse(unit);
        assertThat(durationUnit).isEqualTo(expectedDuration);
    }

    @Test
    public void unknown_unit_should_raise() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> DurationUnit.parse("whatever"))
            .withMessage("Unknown time unit whatever; expected values : (ns|\u03bcs|\u00b5s|ms|sec|s|min|m|hours|hour|h|hour(s)|day(s)|d|days|day)");
    }
}
