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

package com.chutneytesting.glacio.domain.parser.executable.specific.strategy;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.glacio.domain.parser.strategy.StrategyRetryParser;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class StrategyRetryParserTest {

    final StrategyRetryParser sut = new StrategyRetryParser();

    @Test
    void should_parse_parameters() {
        Map<String, Object> expected = new HashMap<>(2);
        expected.put("timeOut", "1 s");
        expected.put("retryDelay", "2 s");

        StepDefinitionDto.StrategyPropertiesDto strategyProperties = sut.parseProperties(Locale.ENGLISH, "every 1 s for 2 s");

        Assertions.assertThat(strategyProperties).isEqualTo(expected);

    }

    @Test
    void should_parse_lang_parameters() {
        Map<String, Object> expected = new HashMap<>(2);
        expected.put("timeOut", "1 s");
        expected.put("retryDelay", "2 s");

        StepDefinitionDto.StrategyPropertiesDto strategyProperties = sut.parseProperties(Locale.FRENCH, "toutes les 1 s pendant 2 s");

        Assertions.assertThat(strategyProperties).isEqualTo(expected);

    }

    @Test
    void should_parse_parameters_2() {
        Map<String, Object> expected = new HashMap<>(2);
        expected.put("timeOut", "5 s");
        expected.put("retryDelay", "3 s");

        StepDefinitionDto.StrategyPropertiesDto strategyProperties = sut.parseProperties(Locale.ENGLISH, "this are free words every 5 s noise noise words for 3 s still noise ");

        Assertions.assertThat(strategyProperties).isEqualTo(expected);

    }
}
