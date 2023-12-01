/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
