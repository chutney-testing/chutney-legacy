package com.chutneytesting.design.api.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtStep;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

public class OldFormatAdapterTest {

    private String getScenarioContent(String fileName) {
        return Files.contentOf(new File(OldFormatAdapterTest.class.getResource("/raw_scenarios/"+ fileName).getPath()), StandardCharsets.UTF_8);
    }

    @Test
    public void should_keep_all_steps_when_scenario_v1_uses_multiple_when_steps() {
        // Given
        OldFormatAdapter.Convertible scenarioV1 = OldFormatAdapter.fromV1(getScenarioContent("scenario.v1.multiple_when.json"));
        List<GwtStep> expectedSteps = ((OldFormatAdapter.ScenarioV1) scenarioV1).rootStep.steps.stream()
            .map(OldFormatAdapter.ScenarioV1.StepV1::toGwt)
            .collect(Collectors.toList());

        // When
        GwtScenario actualScenario = scenarioV1.toGwt("", "");

        // Then
        assertThat(actualScenario.steps().size()).isEqualTo(expectedSteps.size());
        assertThat(actualScenario.steps()).isEqualTo(expectedSteps);
    }

    @Test
    public void should_keep_all_steps_as_given_when_scenario_v1_does_not_have_a_when_step() {
        // Given
        OldFormatAdapter.Convertible scenarioV1 = OldFormatAdapter.fromV1(getScenarioContent("scenario.v1.no_when.json"));
        List<GwtStep> expectedSteps = ((OldFormatAdapter.ScenarioV1) scenarioV1).rootStep.steps.stream()
            .map(OldFormatAdapter.ScenarioV1.StepV1::toGwt)
            .collect(Collectors.toList());

        // When
        GwtScenario actualScenario = scenarioV1.toGwt("", "");

        // Then
        assertThat(actualScenario.steps().size()).isEqualTo(expectedSteps.size()+1);
        assertThat(actualScenario.givens).isEqualTo(expectedSteps);
    }
}
