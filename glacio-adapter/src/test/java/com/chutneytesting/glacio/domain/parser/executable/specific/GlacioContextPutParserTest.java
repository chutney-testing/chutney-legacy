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

import static com.chutneytesting.glacio.domain.parser.GlacioParserHelper.buildDataTableStepWithText;
import static com.chutneytesting.glacio.domain.parser.GlacioParserHelper.buildSimpleStepWithText;
import static com.chutneytesting.glacio.domain.parser.GlacioParserHelper.buildSubStepsStepWithText;
import static com.chutneytesting.glacio.domain.parser.GlacioParserHelper.loopOverRandomString;
import static com.chutneytesting.glacio.domain.parser.ParsingContext.PARSING_CONTEXT_KEYS.ENVIRONMENT;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class GlacioContextPutParserTest {

    private static final ParsingContext CONTEXT = new ParsingContext();
    private static final StepDefinitionDto.StepStrategyDefinitionDto NO_STRATEGY_DEF = null;

    private final GlacioContextPutParser sut = new GlacioContextPutParser();

    @BeforeAll
    public static void setUp() {
        CONTEXT.values.put(ENVIRONMENT, "ENV");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "add",
        "put",
        "store"
    })
    public void keywords(String keyword) {
        assertThat(sut.keywords().get(Locale.ENGLISH)).contains(keyword);
        assertThat(sut.keywords().get(Locale.ENGLISH)).contains(capitalize(keyword));
    }

    @Test
    public void should_parse_only_context_put_action() {
        String sleepActionType = "context-put";
        loopOverRandomString(10, 30, 30, (randomString) ->
            assertThat(
                sut.parseActionType(buildSimpleStepWithText(randomString)))
                .isEqualTo(sleepActionType)
        );
    }

    @Test
    public void should_parse_entries_input_from_step_datatable() {
        String dataTableString = "| var1 | val ue1 |\n| var 2 | value2 |";
        assertThat(
            sut.mapToStepDefinition(CONTEXT, buildDataTableStepWithText("add variables", dataTableString), NO_STRATEGY_DEF).inputs)
            .containsExactly(entry("entries", Map.of("var1", "val ue1", "var 2", "value2")));
    }

    @Test
    public void should_parse_entries_input_from_step_substeps_without_spaces() {
        String subStepsString = "var1 value1\nvar2 value2";
        assertThat(
            sut.mapToStepDefinition(CONTEXT, buildSubStepsStepWithText("add variables", subStepsString), NO_STRATEGY_DEF).inputs)
            .containsExactly(entry("entries", Map.of("var1", "value1", "var2", "value2")));
    }

    @Test
    public void should_parse_entries_input_from_step_substeps_with_spaces() {
        String subStepsString = "var1 val ue1\n\"var 2\" value2";
        assertThat(
            sut.mapToStepDefinition(CONTEXT, buildSubStepsStepWithText("add variables", subStepsString), NO_STRATEGY_DEF).inputs)
            .containsExactly(entry("entries", Map.of("var1", "val ue1", "var 2", "value2")));
    }
}
