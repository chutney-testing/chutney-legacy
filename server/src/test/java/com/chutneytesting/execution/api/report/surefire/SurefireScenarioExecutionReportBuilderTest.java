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

package com.chutneytesting.execution.api.report.surefire;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.execution.api.report.surefire.Testsuite.Testcase;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionCampaign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SurefireScenarioExecutionReportBuilderTest {

    private ExecutionHistoryRepository executionHistoryRepository = mock(ExecutionHistoryRepository.class);
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final SurefireScenarioExecutionReportBuilder sut = new SurefireScenarioExecutionReportBuilder(objectMapper, executionHistoryRepository);

    @Test
    public void create_with_report_OK() throws JsonProcessingException {
        // Given
        StepExecutionReportCore successStepReport =
            stepReport("root step Title", -1L, ServerReportStatus.SUCCESS,
                stepReport("step 1", 24L, ServerReportStatus.SUCCESS,
                    stepReport("step1.1", 23L, ServerReportStatus.SUCCESS)));

        ScenarioExecutionReport report = new ScenarioExecutionReport(1L, "scenario name", "", "", successStepReport);
        ExecutionHistory.Execution execution = ImmutableExecutionHistory.Execution
            .builder()
            .executionId(report.executionId)
            .duration(320L)
            .status(ServerReportStatus.SUCCESS)
            .time(LocalDateTime.now())
            .report(objectMapper.writeValueAsString(report))
            .testCaseTitle("fake")
            .environment("")
            .user("user")
            .scenarioId("")
            .build();

        ScenarioExecutionCampaign scenarioExecutionCampaign = new ScenarioExecutionCampaign("123", "test1", execution.summary());
        when(executionHistoryRepository.getExecution(scenarioExecutionCampaign.scenarioId, report.executionId)).thenReturn(execution);

        // When
        Testsuite testsuite = sut.create(scenarioExecutionCampaign);

        // Then
        assertThat(testsuite.getName()).isEqualTo("123_test1");
        assertThat(testsuite.getTime()).isEqualTo("0.32");
        assertThat(testsuite.getTests()).isEqualTo("2");
        assertThat(testsuite.getFailures()).isEqualTo("0");
        assertThat(testsuite.getSkipped()).isEqualTo("0");
        assertThat(testsuite.getErrors()).isEqualTo("0");

        assertThat(testsuite.getTestcase()).hasSize(2);

        Testcase step1 = testsuite.getTestcase().get(0);
        assertThat(step1.getName()).isEqualTo("1 - step 1");
        assertThat(step1.getTime()).isEqualTo("0.024");
        assertThat(step1.getFailure()).hasSize(0);
        assertThat(step1.getSkipped()).isNull();
        assertThat(step1.getSystemOut().getValue()).isEqualTo("test info");

        Testcase step2 = testsuite.getTestcase().get(1);
        assertThat(step2.getName()).isEqualTo("1.1 - step 1 / step1.1");
        assertThat(step2.getTime()).isEqualTo("0.023");
        assertThat(step2.getFailure()).hasSize(0);
        assertThat(step2.getSkipped()).isNull();
        assertThat(step2.getSystemOut().getValue()).isEqualTo("test info");
    }

    @Test
    public void create_with_report_KO() throws JsonProcessingException {
        // Given
        StepExecutionReportCore failureStepReport =
            stepReport("root step Title", -1L, ServerReportStatus.FAILURE,
                stepReport("step1", 23L, ServerReportStatus.SUCCESS),
                stepReport("step2", 420L, ServerReportStatus.FAILURE),
                stepReport("step3", 0L, ServerReportStatus.NOT_EXECUTED));

        ScenarioExecutionReport report = new ScenarioExecutionReport(1L, "scenario name", "", "", failureStepReport);
        ExecutionHistory.Execution execution = ImmutableExecutionHistory.Execution
            .builder()
            .executionId(report.executionId)
            .duration(23546L)
            .status(ServerReportStatus.FAILURE)
            .time(LocalDateTime.now())
            .report(objectMapper.writeValueAsString(report))
            .testCaseTitle("fake")
            .environment("")
            .user("user")
            .scenarioId("")
            .build();

        ScenarioExecutionCampaign scenarioExecutionCampaign = new ScenarioExecutionCampaign("123", "test2", execution.summary());
        when(executionHistoryRepository.getExecution(scenarioExecutionCampaign.scenarioId, report.executionId)).thenReturn(execution);

        // When
        Testsuite testsuite = sut.create(scenarioExecutionCampaign);

        // Then
        assertThat(testsuite.getName()).isEqualTo("123_test2");
        assertThat(testsuite.getTime()).isEqualTo("23.546");
        assertThat(testsuite.getTests()).isEqualTo("3");
        assertThat(testsuite.getFailures()).isEqualTo("1");
        assertThat(testsuite.getSkipped()).isEqualTo("1");
        assertThat(testsuite.getErrors()).isEqualTo("0");

        assertThat(testsuite.getTestcase()).hasSize(3);

        Testcase step1 = testsuite.getTestcase().get(0);
        assertThat(step1.getName()).isEqualTo("1 - step1");
        assertThat(step1.getTime()).isEqualTo("0.023");
        assertThat(step1.getFailure()).hasSize(0);
        assertThat(step1.getSkipped()).isNull();
        assertThat(step1.getSystemOut().getValue()).isEqualTo("test info");

        Testcase step2 = testsuite.getTestcase().get(1);
        assertThat(step2.getName()).isEqualTo("2 - step2");
        assertThat(step2.getTime()).isEqualTo("0.42");
        assertThat(step2.getFailure()).hasSize(1);
        assertThat(step2.getFailure().get(0).message).isEqualTo("test error");
        assertThat(step2.getSkipped()).isNull();
        assertThat(step2.getSystemOut()).isNull();

        Testcase step3 = testsuite.getTestcase().get(2);
        assertThat(step3.getName()).isEqualTo("3 - step3");
        assertThat(step3.getTime()).isEqualTo("0.0");
        assertThat(step3.getFailure()).hasSize(0);
        assertThat(step3.getSkipped()).isNotNull();
        assertThat(step3.getSkipped().getValue().message).isEqualTo("Not executed");
        assertThat(step2.getSystemOut()).isNull();
    }

    private StepExecutionReportCore stepReport(String title, long duration, ServerReportStatus status, StepExecutionReportCore... subSteps) {
        List<String> infos = ServerReportStatus.SUCCESS == status ? singletonList("test info") : emptyList();
        List<String> errors = ServerReportStatus.FAILURE == status ? singletonList("test error") : emptyList();

        return new StepExecutionReportCore(
            title,
            duration,
            Instant.now(),
            status,
            infos,
            errors,
            Arrays.asList(subSteps),
            "type",
            "targetName",
            "targetUrl",
            "strategy",
            Maps.newHashMap(),
            Maps.newHashMap());
    }

}
