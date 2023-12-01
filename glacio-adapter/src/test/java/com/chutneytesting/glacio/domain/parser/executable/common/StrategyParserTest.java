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

import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.glacio.domain.parser.strategy.StrategyParser;
import com.chutneytesting.glacio.domain.parser.strategy.StrategySoftAssertParser;
import com.github.fridujo.glacio.model.Step;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class StrategyParserTest {

    private final StrategyParser sut = new StrategyParser();

    /*

    Scenario: Gracefully fallback to default strategy when type is unknown
        When A step fails (unkown-strat:)
            Do fail
        Then it keeps going to the next step
            Do success
    * */

    @Test
    void should_find_ending_strategy_without_parameters() {
        String sentence = "a step sentence with strategy (strategy:)";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).containsKey("strategy");
        assertThat(actualStrategyDefinition.get("strategy")).isEqualTo("");
    }

    @Test
    void should_find_strategy_anywhere_in_a_sentence() {
        String sentence = "a step (anywhere:) sentence";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).containsKeys("anywhere");
        assertThat(actualStrategyDefinition.get("anywhere")).isEqualTo("");
    }

    @Test
    void should_not_find_any_strategy() {
        String sentence = "a step (this is just a comment) sentence  with (another-non-catchable(: comment)";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).isEmpty();
    }

    @Test
    void should_find_two_strategies() {
        String sentence = "(softly:) Do a step (with a parenthesised comment) using 2 strategies (retry:) (should(: not be catch)";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).hasSize(2);
        assertThat(actualStrategyDefinition).containsKeys("softly", "retry");
    }

    @Test
    void should_find_strategy_with_hyphen_or_underscore_names() {
        String sentence = "a step sentence with (under_score_strategy:) and (hyphen-strategy:)";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).containsKeys("under_score_strategy", "hyphen-strategy");
    }

    @Test
    void should_find_parser_for_soft_assert_strategy() {
        StrategyParser sut = new StrategyParser(Arrays.asList(new StrategySoftAssertParser()));

        StrategyParser strategyParser = sut.getStrategyParser(ENGLISH, "softly");

        assertThat(strategyParser).isInstanceOf(StrategySoftAssertParser.class);
    }

    @Test
    void should_remove_strategies_from_step_sentence_to_avoid_later_conflict_when_parsing_inputs_parameters() {
        Step step = new Step(false, Optional.empty(), "(softly:) Do a step (with a parenthesised comment) using 2 strategies (retry:) (should(: not be catch)", Optional.empty(), emptyList());
        StrategyParser sut = new StrategyParser(Arrays.asList(new StrategySoftAssertParser()));

        Step actual = sut.stripStrategyFrom(step);

        assertThat(actual.getText()).isEqualToIgnoringCase("Do a step (with a parenthesised comment) using 2 strategies (should(: not be catch)");
    }

}
