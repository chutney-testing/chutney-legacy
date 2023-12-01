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

package com.chutneytesting.glacio.domain.parser.strategy;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.action.spi.time.DurationUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrategyRetryParser extends StrategyParser {

    private final String EN_RETRY_PARAMETERS_PATTERN_STRING = "(?<timeout>every\\s+(?<tval>.*))(?<delay>(for|until)\\s+(?<dval>.*))";
    private final String FR_RETRY_PARAMETERS_PATTERN_STRING = "(?<timeout>toutes les\\s+(?<tval>.*))(?<delay>(pendant|jusqu'à)\\s+(?<dval>.*))";

    private final Map<Locale, Set<String>> keywords = new HashMap<>(2);
    private final Map<Locale, Pattern> patterns = new HashMap<>(2);

    public StrategyRetryParser() {
        keywords.put(Locale.ENGLISH,
            new HashSet<>(Arrays.asList("retry", "retry-with-timeout", "retry_with_timeout")));
        keywords.put(Locale.FRENCH,
            new HashSet<>(Arrays.asList("refait", "rejoue", "relance", "recommence")));

        patterns.put(Locale.ENGLISH, Pattern.compile(EN_RETRY_PARAMETERS_PATTERN_STRING));
        patterns.put(Locale.FRENCH, Pattern.compile(FR_RETRY_PARAMETERS_PATTERN_STRING));
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        return keywords;
    }

    @Override
    public StepDefinitionDto.StepStrategyDefinitionDto toStrategyDef(Locale lang, String parameters) {
        return new StepDefinitionDto.StepStrategyDefinitionDto("retry-with-timeout", parseProperties(lang, parameters));
    }

    @Override
    public StepDefinitionDto.StrategyPropertiesDto parseProperties(Locale lang, String parameters) {
        Map<String, Object> params = new HashMap<>(2);
        Pattern pattern = patterns.get(lang);
        Matcher matcher = pattern.matcher(parameters.trim().replaceAll("\\s+", " "));

        while (matcher.find()) {
            ofNullable(matcher.group("timeout")).ifPresent(t -> params.put("timeOut", DurationParser.parseDurationValue(matcher.group("tval"))));
            ofNullable(matcher.group("delay")).ifPresent(t -> params.put("retryDelay", DurationParser.parseDurationValue(matcher.group("dval"))));
        }

        return new StepDefinitionDto.StrategyPropertiesDto(params);
    }

    static class DurationParser {
        private static final String SIMPLE_DURATION_REGEX = "(?<param>(?<value>\\d+(?:[.,]\\d+)?)\\s+(?<unit>" + DurationUnit.regex() + "))";
        private static final Pattern SIMPLE_DURATION_PATTERN = Pattern.compile(SIMPLE_DURATION_REGEX);

        private static String parseDurationValue(String literalDuration) {
            Matcher matcher = SIMPLE_DURATION_PATTERN.matcher(literalDuration.trim().toLowerCase());
            if (matcher.find()) {
                return matcher.group("param");
            }

            return "";
        }
    }
}
