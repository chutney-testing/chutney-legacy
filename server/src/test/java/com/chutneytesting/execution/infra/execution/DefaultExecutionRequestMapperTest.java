package com.chutneytesting.execution.infra.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.scenario.domain.raw.RawTestCase;
import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class DefaultExecutionRequestMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final EmbeddedEnvironmentApi environmentApplication = mock(EmbeddedEnvironmentApi.class);
    private final CurrentNetworkDescription currentNetworkDescription = mock(CurrentNetworkDescription.class);

    private final DefaultExecutionRequestMapper sut = new DefaultExecutionRequestMapper(objectMapper, environmentApplication, currentNetworkDescription);

    @Test
    public void should_map_test_case_to_execution_request() {
        // Given
        RawTestCase testCase = RawTestCase.builder()
            .withScenario(Files.contentOf(new File(DefaultExecutionRequestMapperTest.class.getResource("/raw_scenarios/scenario.json").getPath()), StandardCharsets.UTF_8))
            .build();
        ExecutionRequest request = new ExecutionRequest(testCase, "", "", DataSet.builder().build());

        // When
        ExecutionRequestDto executionRequestDto = sut.toDto(request);

        // Then
        assertThat(executionRequestDto.scenario).isNotNull();
        assertThat(executionRequestDto.scenario.name).isEqualTo("root step");
        assertThat(executionRequestDto.scenario.steps.get(0).name).isEqualTo("context-put name");
        assertThat(executionRequestDto.scenario.steps.get(0).inputs).containsKey("someID");
    }

}
