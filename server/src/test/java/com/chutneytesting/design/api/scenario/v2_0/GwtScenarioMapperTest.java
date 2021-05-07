package com.chutneytesting.design.api.scenario.v2_0;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.api.scenario.v2_0.mapper.GwtScenarioMapper;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.execution.domain.compiler.GwtScenarioMarshaller;
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
