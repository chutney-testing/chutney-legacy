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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class UntilHourDurationParserTest {

    private static final Clock midnightClock = Clock.fixed(Instant.now().truncatedTo(ChronoUnit.DAYS), ZoneId.of("UTC"));
    private final DurationParser sut = new UntilHourDurationParser(midnightClock);

    @ParameterizedTest(name = "{0} is parsed to {1} ms")
    @MethodSource("parametersForParsing_nominal_cases")
    public void parsing_nominal_cases(String literalDuration, long expectedMilliseconds) {
        Optional<Duration> optionalDuration = sut.parse(literalDuration);
        assertThat(optionalDuration).as("Parsing result").isPresent();

        Duration duration = optionalDuration.get();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(duration.toMilliseconds()).isEqualTo(expectedMilliseconds);
        softly.assertThat(duration.toString()).isEqualTo(duration.toMilliseconds() + " ms");
        softly.assertAll();
    }

    public static Object[] parametersForParsing_nominal_cases() {
        return new Object[][]{
            {" until 02:00 ", 2 * 3600000},
            {"until   19:37 ", (19 * 3600000) + (37 * 60000)},
        };
    }

    @ParameterizedTest(name = "{0} is not valid")
    @MethodSource("parametersForUnmatched_parsing")
    public void unmatched_parsing(String literalDuration) {
        Optional<Duration> optionalDuration = sut.parse(literalDuration);
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
        assertThat(sut.description()).isEqualTo("Until hour: until <1..24>:<1..60>\n" +
            "Samples:\n" +
            "\t until 02:00\n" +
            "\t until 17:45");
    }
}
