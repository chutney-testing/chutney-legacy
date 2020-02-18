package com.chutneytesting.task.spi.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class DurationUnitTest {


    public static Object[] data() {
        return new Object[][]{
            {"   ms  ", DurationUnit.MILLIS},
            {"s", DurationUnit.SECONDS},
            {"SeC ", DurationUnit.SECONDS},
            {"m", DurationUnit.MINUTES},
            {"mIn", DurationUnit.MINUTES}
        };
    }

    @Test
    @Parameters(method = "data")
    public void should_parse_unit(String unit, DurationUnit expectedDuration) {
        DurationUnit durationUnit = DurationUnit.parse(unit);
        assertThat(durationUnit).isEqualTo(expectedDuration);
    }

    @Test
    public void unknown_unit_should_raise() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> DurationUnit.parse("whatever"))
            .withMessage("Unknown time unit whatever; expected values : (ms|sec|s|min|m|hours|hour|h|hour(s)|day(s)|d|days|day)");
    }
}
