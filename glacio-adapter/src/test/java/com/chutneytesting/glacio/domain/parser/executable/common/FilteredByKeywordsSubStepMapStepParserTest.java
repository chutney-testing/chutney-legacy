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

import static com.chutneytesting.glacio.domain.parser.ParsingContext.PARSING_CONTEXT_KEYS.ENVIRONMENT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.glacio.domain.parser.GlacioStepParser;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.github.fridujo.glacio.model.Step;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class FilteredByKeywordsSubStepMapStepParserTest {

    private static final ParsingContext parsingContext = new ParsingContext();
    private FilteredByKeywordsSubStepMapStepParser sut;
    private GlacioStepParser<Map.Entry<String, Object>> entryStepParser;

    @BeforeEach
    public void setUp() {
        parsingContext.values.put(ENVIRONMENT, "ENV");
        entryStepParser = mock(GlacioStepParser.class);
    }

    @Test
    public void should_filter_substeps_with_keywords_and_collect_results_from_delegate_to_entry_parser() {
        // Given
        Step step = mock(Step.class);
        Step firstStep = new Step(false, empty(), "TEST step name 1", empty(), emptyList());
        Step thirdStep = new Step(false, empty(), "TST step name 3", empty(), emptyList());
        when(step.getSubsteps())
            .thenReturn(asList(
                firstStep,
                new Step(false, empty(), "TTT step name", empty(), emptyList()),
                thirdStep
            ));
        when(entryStepParser.parseGlacioStep(any(), any())).thenReturn(entry("one", "v1"), entry("two", "v2"));

        sut = new FilteredByKeywordsSubStepMapStepParser(entryStepParser, "TEST", "TST");

        // When / Then
        assertThat(sut.parseGlacioStep(parsingContext, step)).containsOnly(entry("one", "v1"), entry("two", "v2"));

        ArgumentCaptor<Step> stepArgumentCaptor = ArgumentCaptor.forClass(Step.class);
        verify(entryStepParser, times(2)).parseGlacioStep(any(), stepArgumentCaptor.capture());
        assertThat(stepArgumentCaptor.getAllValues())
            .extracting(Step::getText)
            .containsExactlyInAnyOrder("step name 1", "step name 3");
    }
}
