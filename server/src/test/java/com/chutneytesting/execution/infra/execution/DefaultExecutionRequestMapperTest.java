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

package com.chutneytesting.execution.infra.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.environment.api.target.EmbeddedTargetApi;
import com.chutneytesting.scenario.domain.raw.RawTestCase;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class DefaultExecutionRequestMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final EmbeddedTargetApi embeddedTargetApi = mock(EmbeddedTargetApi.class);
    private final CurrentNetworkDescription currentNetworkDescription = mock(CurrentNetworkDescription.class);

    private final DefaultExecutionRequestMapper sut = new DefaultExecutionRequestMapper(objectMapper, embeddedTargetApi, currentNetworkDescription);

    @Test
    public void should_map_test_case_to_execution_request() {
        // Given
        RawTestCase testCase = RawTestCase.builder()
            .withScenario(Files.contentOf(new File(DefaultExecutionRequestMapperTest.class.getResource("/raw_scenarios/scenario.json").getPath()), StandardCharsets.UTF_8))
            .build();
        ExecutionRequest request = new ExecutionRequest(testCase, "", "");

        // When
        ExecutionRequestDto executionRequestDto = sut.toDto(request);

        // Then
        assertThat(executionRequestDto.scenario).isNotNull();
        assertThat(executionRequestDto.scenario.name).isEqualTo("root step");
        assertThat(executionRequestDto.scenario.steps.get(0).name).isEqualTo("context-put name");
        assertThat(executionRequestDto.scenario.steps.get(0).inputs).containsKey("someID");
    }

}
