package com.chutneytesting.design.api.scenario.v2_0;

import static com.google.common.io.Resources.getResource;
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
        String rawScenario = Files.contentOf(new File(getResource("raw_scenarios/raw_scenario_json_with_x-$ref.json").getPath()), StandardCharsets.UTF_8);

        // When: deserialize into GwtScenario
        GwtScenario actualScenario = marshaller.deserialize("a title", "a description", rawScenario);

        //Then:
        assertThat(actualScenario.givens.size()).isEqualTo(2);
        assertThat(actualScenario.givens.get(0).xRef).hasValue("common/frag1.icefrag.json");
        assertThat(actualScenario.givens.get(1).implementation.get().xRef).isEqualTo("common/frag2.icefrag.json");

    }

}
