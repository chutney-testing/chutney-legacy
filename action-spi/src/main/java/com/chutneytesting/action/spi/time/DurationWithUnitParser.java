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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DurationWithUnitParser implements DurationParser {

    private static final String SIMPLE_DURATION_REGEX = "^(?<value>\\d+(?:[.,]\\d+)?)\\s+(?<unit>" + DurationUnit.regex() + ")$";

    private static final Pattern SIMPLE_DURATION_PATTERN = Pattern.compile(SIMPLE_DURATION_REGEX);

    @Override
    public Optional<Duration> parse(String literalDuration) {
        Matcher matcher = SIMPLE_DURATION_PATTERN.matcher(literalDuration.trim().toLowerCase());
        final Optional<Duration> parsingResult;
        if (matcher.matches()) {
            double value = Double.parseDouble(matcher.group("value").replace(',', '.'));
            DurationUnit unit = DurationUnit.parse(matcher.group("unit"));
            parsingResult = Optional.of(new Duration(value, unit));
        } else {
            parsingResult = Optional.empty();
        }

        return parsingResult;
    }

    @Override
    public String description() {
        return "Duration with unit: <positive number> " + DurationUnit.regex() + "\n" +
            "Samples:\n" +
            "\t 3 min\n" +
            "\t 4,5 hours";
    }
}
