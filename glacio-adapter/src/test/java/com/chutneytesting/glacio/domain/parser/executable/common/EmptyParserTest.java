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

import static com.chutneytesting.glacio.domain.parser.GlacioParserHelper.buildSimpleStepWithText;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.github.fridujo.glacio.model.Step;
import org.junit.jupiter.api.Test;

public class EmptyParserTest {

    private static final ParsingContext parsingContext = new ParsingContext();
    private final Step step = buildSimpleStepWithText("");

    @Test
    public void should_give_static_access_to_empty_map_step_parser() {
        assertThat(EmptyParser.emptyMapParser.parseGlacioStep(parsingContext, step)).isEmpty();
    }

    @Test
    public void should_give_static_access_to_no_target_step_parser() {
        assertThat(EmptyParser.noTargetParser.parseGlacioStep(parsingContext, step)).isNull();
    }

    @Test
    public void should_give_static_access_to_no_strategy_step_parser() {
        assertThat(EmptyParser.noStrategyParser.parseGlacioStep(step)).isEmpty();
    }
}
