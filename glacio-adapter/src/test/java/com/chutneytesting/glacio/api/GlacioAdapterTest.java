package com.chutneytesting.glacio.api;

import static com.chutneytesting.glacio.api.GlacioAdapter.DEFAULT_ENV;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.glacio.domain.parser.StepFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
    private StepFactory stepFactory;

    @BeforeEach
    public void setUp() {
        stepFactory = mock(StepFactory.class);
        sut = new GlacioAdapter(stepFactory);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "en /unit/lang_default.feature",
        "fr /unit/lang_fr.feature"
    })
    public void should_use_feature_language_hint_with_english_default(String langFeature) {
        String[] s = langFeature.split(" ");
        Locale expectedLocale = new Locale(s[0]);
        String featureFilePath = s[1];

        String feature = fileContent(featureFilePath);
        StepDefinitionDto fakeStepDef = new StepDefinitionDto("fake", null, "", null, Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
        when(stepFactory.toStepDefinition(any(), any(), any())).thenReturn(fakeStepDef);

        List<StepDefinitionDto> stepDefinitions = sut.toChutneyStepDefinition(feature, null);

        assertThat(stepDefinitions).hasSize(1);
        verify(stepFactory).toStepDefinition(eq(expectedLocale), any(), any());
    }

    @Test
    public void should_adapt_as_many_glacio_scenarios_as_feature_has() {
        String featureName = "Feature with multiple scenarios";
        String feature = fileContent("/unit/multiple_scenarios.feature");
        List<StepDefinitionDto> stepDefinitions = sut.toChutneyStepDefinition(feature);
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
    public void should_delegate_step_creation_for_all_first_level_steps() {
        when(stepFactory.toStepDefinition(any(), any(), any())).thenReturn(new StepDefinitionDto("fake", null, "", null, Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()));
        String feature = fileContent("/unit/multiple_non_executable_steps.feature");
        sut.toChutneyStepDefinition(feature, ENVIRONMENT);
        verify(stepFactory, times(2)).toStepDefinition(eq(Locale.ENGLISH), any(), any());
    }

    private String fileContent(String resourcePath) {
        return Files.contentOf(new File(GlacioAdapterTest.class.getResource(resourcePath).getPath()), StandardCharsets.UTF_8);
    }
}
