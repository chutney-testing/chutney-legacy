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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.github.fridujo.glacio.model.Step;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class NoStrategyParser implements IParseStrategy {

    @Override
    public Map<Locale, Set<String>> keywords() {
        return emptyMap();
    }

    @Override
    public List<StepDefinitionDto.StepStrategyDefinitionDto> parseGlacioStep(Locale lang, Step step) {
        return emptyList();
    }

    @Override
    public Pair<Step, List<StepDefinitionDto.StepStrategyDefinitionDto>> parseStepAndStripStrategy(Locale lang, Step step) {
        return Pair.of(step, emptyList());
    }
}
