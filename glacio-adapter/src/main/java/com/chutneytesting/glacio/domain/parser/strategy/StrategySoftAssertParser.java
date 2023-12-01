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

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StrategySoftAssertParser extends StrategyParser {

    private final Map<Locale, Set<String>> keywords = new HashMap<>(2);

    public StrategySoftAssertParser() {
        keywords.put(Locale.ENGLISH,
            new HashSet<>(Arrays.asList("soft", "softly")));
        keywords.put(Locale.FRENCH,
            new HashSet<>(Arrays.asList("soft", "softly", "doucement")));
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        return keywords;
    }

    @Override
    public StepDefinitionDto.StepStrategyDefinitionDto toStrategyDef(Locale lang, String parameters) {
        return new StepDefinitionDto.StepStrategyDefinitionDto("soft-assert", parseProperties(lang, parameters));
    }

}
