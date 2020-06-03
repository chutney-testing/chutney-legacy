package test.unit.com.chutneytesting.engine.api.glacio;

import static com.chutneytesting.engine.api.glacio.GlacioAdapter.DEFAULT_ENV;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.api.glacio.ExecutableStepFactory;
import com.chutneytesting.engine.api.glacio.GlacioAdapter;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.google.common.io.Resources;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class GlacioAdapterTest {

    private static final String ENVIRONMENT = DEFAULT_ENV;

    private GlacioAdapter sut;
    private ExecutableStepFactory executableStepFactory;

    @BeforeEach
    public void setUp() {
        executableStepFactory = mock(ExecutableStepFactory.class);
        sut = new GlacioAdapter(executableStepFactory);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "en unit/lang_default.feature",
        "fr unit/lang_fr.feature"
    })
    public void should_use_feature_language_hint_with_english_default(String langFeature) {
        String[] s = langFeature.split(" ");
        Locale expectedLocale = new Locale(s[0]);
        String featureFilePath = s[1];

        String feature = fileContent(featureFilePath);
        when(executableStepFactory.isExecutableStep(any(), any()))
            .thenReturn(true);
        when(executableStepFactory.build(any(), any(), any()))
            .thenReturn(null);

        List<StepDefinition> stepDefinitions = sut.toChutneyStepDefinition(feature, null);

        assertThat(stepDefinitions).hasSize(1);
        verify(executableStepFactory).isExecutableStep(eq(expectedLocale), any());
        verify(executableStepFactory).build(eq(expectedLocale), eq(DEFAULT_ENV), any());
    }

    @Test
    public void should_adapt_as_many_glacio_scenarios_as_feature_has() {
        String featureName = "Feature with multiple scenarios";
        String feature = fileContent("unit/multiple_scenarios.feature");
        List<StepDefinition> stepDefinitions = sut.toChutneyStepDefinition(feature);
        assertThat(stepDefinitions).hasSize(3);
        assertThat(stepDefinitions)
            .extracting(stepDefinition -> stepDefinition.name)
            .containsExactly(
                featureName + " - " + "First scenario",
                featureName + " - " + "Second scenario",
                featureName + " - " + "Third scenario"
            );
    }

    @Test
    public void should_adapt_as_many_non_executable_steps_glacio_scenario_has() {
        String feature = fileContent("unit/multiple_non_executable_steps.feature");
        List<StepDefinition> stepDefinitions = sut.toChutneyStepDefinition(feature, ENVIRONMENT);
        assertThat(stepDefinitions).hasSize(1);
        assertThat(stepDefinitions.get(0).steps).hasSize(2);

        StepDefinition whenStepDefinition = stepDefinitions.get(0).steps.get(0);
        assertSizeAndName(whenStepDefinition, "When We try something", 3);
        assertSizeAndName(whenStepDefinition.steps.get(0), "First substep of action step", 0);
        assertSizeAndName(whenStepDefinition.steps.get(1), "Second substep of action step", 1);
        assertSizeAndName(whenStepDefinition.steps.get(2), "Third substep of action step", 2);

        StepDefinition thenStepDefinition = stepDefinitions.get(0).steps.get(1);
        assertSizeAndName(thenStepDefinition, "Then An assert is ok", 3);
        assertSizeAndName(thenStepDefinition.steps.get(0), "First substep of assert step", 2);
        assertSizeAndName(thenStepDefinition.steps.get(1), "Second substep of assert step", 1);
        assertSizeAndName(thenStepDefinition.steps.get(2), "Third substep of assert step", 0);
    }

    @Test
    public void should_delegate_test_for_executable_step_for_each_glacio_step() {
        String feature = fileContent("unit/multiple_non_executable_steps.feature");
        sut.toChutneyStepDefinition(feature, ENVIRONMENT);
        verify(executableStepFactory, times(14)).isExecutableStep(eq(Locale.ENGLISH), any());
    }

    @Test
    public void should_delegate_adaptation_for_executable_steps() {
        String feature = fileContent("unit/multiple_non_executable_steps.feature");
        when(executableStepFactory.isExecutableStep(any(), any()))
            .thenReturn(
                false,                  // When
                true,
                false, true,
                false, true, true,
                false,                  // Then
                true,               // Test that no adaptation get further executable step
                false, true,
                true);
        sut.toChutneyStepDefinition(feature, ENVIRONMENT);
        verify(executableStepFactory, times(12)).isExecutableStep(eq(Locale.ENGLISH), any());
        verify(executableStepFactory, times(7)).build(eq(Locale.ENGLISH), eq(ENVIRONMENT), any());
    }

    private void assertSizeAndName(StepDefinition step, String expectedName, Integer expectedSize) {
        assertThat(step.name).as("StepDefinition name").isEqualTo(expectedName);
        assertThat(step.steps).as("StepDefinition substeps size").hasSize(expectedSize);
    }

    private String fileContent(String resourcePath) {
        return Files.contentOf(new File(Resources.getResource(resourcePath).getPath()), StandardCharsets.UTF_8);
    }
}
