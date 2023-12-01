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

package com.chutneytesting.glacio.domain.parser.executable.common;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.glacio.domain.parser.GlacioStepParser;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.chutneytesting.glacio.domain.parser.strategy.IParseStrategy;
import com.chutneytesting.glacio.domain.parser.strategy.NoStrategyParser;
import com.github.fridujo.glacio.model.Step;
import java.util.Map;

public final class EmptyParser {

    public static final GlacioStepParser<Map<String, Object>> emptyMapParser = new EmptyMapParser();
    public static final IParseStrategy noStrategyParser = new NoStrategyParser();
    public static final GlacioStepParser<TargetExecutionDto> noTargetParser = new NoTargetParser();

    private EmptyParser() {
    }

    private static class EmptyMapParser implements GlacioStepParser<Map<String, Object>> {
        @Override
        public Map<String, Object> parseGlacioStep(ParsingContext context, Step step) {
            return emptyMap();
        }
    }

    private static class NoTargetParser implements GlacioStepParser<TargetExecutionDto> {
        @Override
        public TargetExecutionDto parseGlacioStep(ParsingContext context, Step step) {
            return null;
        }
    }
}
