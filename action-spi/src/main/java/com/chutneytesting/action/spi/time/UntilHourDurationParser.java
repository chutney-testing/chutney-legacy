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

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UntilHourDurationParser implements DurationParser {

    private static final String UNTIL_HOURS_REGEX = "^until\\s+(?<hours>[0-2]\\d):(?<minutes>[0-5]\\d)$";
    private static final Pattern UNTIL_HOURS_PATTERN = Pattern.compile(UNTIL_HOURS_REGEX);

    private final Clock clock;

    UntilHourDurationParser() {
        this(Clock.systemDefaultZone());
    }

    UntilHourDurationParser(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<Duration> parse(String literalDuration) {
        Matcher matcher = UNTIL_HOURS_PATTERN.matcher(literalDuration.trim().toLowerCase());
        final Optional<Duration> parsingResult;
        if (matcher.matches()) {
            int hours = Integer.parseInt(matcher.group("hours"));
            int minutes = Integer.parseInt(matcher.group("minutes"));

            LocalDateTime now = LocalDateTime.now(clock);
            LocalDateTime targetTime = now.truncatedTo(ChronoUnit.DAYS).plusHours(hours).plusMinutes(minutes);
            if (targetTime.isBefore(now)) {
                targetTime = targetTime.plusDays(1);
            }
            long durationInMs = java.time.Duration.between(now, targetTime).toMillis();
            parsingResult = Optional.of(new Duration(durationInMs, DurationUnit.MILLIS));
        } else {
            parsingResult = Optional.empty();
        }

        return parsingResult;
    }

    @Override
    public String description() {
        return "Until hour: until <1..24>:<1..60>\n" +
            "Samples:\n" +
            "\t until 02:00\n" +
            "\t until 17:45";
    }
}
