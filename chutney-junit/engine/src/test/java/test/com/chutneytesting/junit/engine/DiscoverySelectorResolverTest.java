package test.com.chutneytesting.junit.engine;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.glacio.api.GlacioAdapter;
import com.chutneytesting.junit.engine.ChutneyEngineDescriptor;
import com.chutneytesting.junit.engine.DiscoverySelectorResolver;
import com.chutneytesting.junit.engine.FeatureDescriptor;
import com.chutneytesting.junit.engine.ScenarioDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.FileSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.chutneytesting.junit.engine.DiscoverySelectorResolver.FEATURE_SEGMENT_TYPE;
import static com.chutneytesting.junit.engine.DiscoverySelectorResolver.SCENARIO_SEGMENT_TYPE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscoverySelectorResolverTest {

    @Test
    void should_add_feature_and_scenario_descriptors_when_resolve() {
        // Given
        GlacioAdapter glacioAdapter = mock(GlacioAdapter.class);
        List<StepDefinitionDto> stepDefinitions = buildFeatureResultParsing("scenario one", "scenario two");
        when(glacioAdapter.toChutneyStepDefinition(any())).thenReturn(stepDefinitions);

        EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
        String fileName = "success.feature";
        when(discoveryRequest.getSelectorsByType(FileSelector.class))
            .thenReturn(singletonList(selectFile("src/test/resources/features/simple/" + fileName)));

        UniqueId engineUniqueId = UniqueId.forEngine("engine-id");
        ChutneyEngineDescriptor engineDescriptor = new ChutneyEngineDescriptor(engineUniqueId, "engineName", null);

        // When
        DiscoverySelectorResolver sut = new DiscoverySelectorResolver(glacioAdapter);
        sut.resolveSelectors(discoveryRequest, engineDescriptor);

        // Then
        Optional<? extends TestDescriptor> featureDescriptor = engineDescriptor.findByUniqueId(engineUniqueId.append(FEATURE_SEGMENT_TYPE, fileName));
        assertThat(featureDescriptor).hasValueSatisfying(fd -> {
            assertThat(fd).isInstanceOf(FeatureDescriptor.class);
            assertThat(fd.isContainer()).isTrue();
            assertThat(fd.isTest()).isFalse();
            assertThat(fd.getDisplayName()).isEqualTo(fileName);
        });

        stepDefinitions.forEach(stepDefinition -> {
            Optional<? extends TestDescriptor> scenarioDescriptor =
                engineDescriptor.findByUniqueId(featureDescriptor.get().getUniqueId().append(SCENARIO_SEGMENT_TYPE, stepDefinition.name));

            assertThat(scenarioDescriptor).hasValueSatisfying(sd -> {
                assertThat(sd).isInstanceOf(ScenarioDescriptor.class);
                assertThat(sd.isContainer()).isFalse();
                assertThat(sd.isTest()).isTrue();
                assertThat(sd.getDisplayName()).isEqualTo(stepDefinition.name);
            });
        });
    }

    private List<StepDefinitionDto> buildFeatureResultParsing(String... scenarioNames) {
        List<StepDefinitionDto> stepDefinitions = new ArrayList<>();
        for (String scenarioName : scenarioNames) {
            stepDefinitions.add(buildStepDefinitionByName(scenarioName));
        }
        return stepDefinitions;
    }

    private StepDefinitionDto buildStepDefinitionByName(String name) {
        return new StepDefinitionDto(name, null, "", null, null, null, null, null);
    }
}
