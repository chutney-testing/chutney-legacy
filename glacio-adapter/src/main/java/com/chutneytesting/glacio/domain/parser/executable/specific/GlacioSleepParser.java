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

package com.chutneytesting.glacio.domain.parser.executable.specific;

import static java.util.Optional.ofNullable;

import com.chutneytesting.glacio.domain.parser.ExecutableGlacioStepParser;
import com.chutneytesting.glacio.domain.parser.GlacioStepParser;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.chutneytesting.glacio.domain.parser.executable.common.EmptyParser;
import com.github.fridujo.glacio.model.Step;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlacioSleepParser extends ExecutableGlacioStepParser {

    private final static Pattern STEP_TEXT_PATTERN = Pattern.compile("^(?:[Ss]leep|[Aa]?[Ww]ait|[Rr]est|[Pp]ause) (?:(for|during) )?(?<duration>.*)$");

    public GlacioSleepParser() {
        super(EmptyParser.noTargetParser,
            new GlacioSleepInputsParser(),
            EmptyParser.emptyMapParser,
            EmptyParser.emptyMapParser);
    }

    @Override
    public String parseActionType(Step step) {
        return "sleep";
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        Map<Locale, Set<String>> keywords = new HashMap<>();
        keywords.put(Locale.ENGLISH,
            new HashSet<>(Arrays.asList("Sleep", "sleep", "Await", "await", "Wait", "wait", "Rest", "rest", "Pause", "pause")));
        return keywords;
    }

    private static class GlacioSleepInputsParser implements GlacioStepParser<Map<String, Object>> {

        @Override
        public Map<String, Object> parseGlacioStep(ParsingContext context, Step step) {
            Matcher matcher = STEP_TEXT_PATTERN.matcher(step.getText());
            if (matcher.matches()) {
                String duration = ofNullable(matcher.group("duration"))
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find duration input in step :" + step));
                return Collections.singletonMap("duration", duration);
            }
            throw new IllegalArgumentException("Cannot match defined pattern : " + STEP_TEXT_PATTERN);
        }
    }

}
