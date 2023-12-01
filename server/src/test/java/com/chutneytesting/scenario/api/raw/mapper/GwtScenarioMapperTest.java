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

package com.chutneytesting.scenario.api.raw.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.execution.domain.GwtScenarioMarshaller;
import com.chutneytesting.scenario.domain.gwt.GwtScenario;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

public class GwtScenarioMapperTest {

    private static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

    @Test
    public void should_deserialize_a_raw_scenario_with_x$ref() {
        // Given raw test v2.1 with x-$ref
        String rawScenario = Files.contentOf(new File(GwtScenarioMapperTest.class.getResource("/raw_scenarios/raw_scenario_json_with_x-$ref.json").getPath()), StandardCharsets.UTF_8);

        // When: deserialize into GwtScenario
        GwtScenario actualScenario = marshaller.deserialize("a title", "a description", rawScenario);

        //Then:
        assertThat(actualScenario.givens.size()).isEqualTo(2);
        assertThat(actualScenario.givens.get(0).xRef).hasValue("common/frag1.icefrag.json");
        assertThat(actualScenario.givens.get(1).implementation.get().xRef).isEqualTo("common/frag2.icefrag.json");

    }

    @Test
    public void should_deserialize_a_raw_scenario() {
        // Given raw
        String rawScenario = Files.contentOf(new File(GwtScenarioMapperTest.class.getResource("/raw_scenarios/scenario_executable.v2.1.json").getPath()), StandardCharsets.UTF_8);

        // When
        GwtScenario actualScenario = marshaller.deserialize("a title", "a description", rawScenario);

        //Then:
        assertThat(actualScenario.givens.size()).isEqualTo(1);
        assertThat(actualScenario.givens.get(0).implementation.get().inputs).containsEntry("fake_param", "fake_value");
        assertThat(actualScenario.givens.get(0).implementation.get().outputs).containsEntry("fake_output", "fake_output_value");
        assertThat(actualScenario.givens.get(0).implementation.get().validations).containsEntry("fake_validation", "${true}");
    }

}
