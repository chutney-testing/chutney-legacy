package com.chutneytesting.execution.api.report.surefire;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.chutneytesting.design.domain.campaign.ScenarioExecutionReportCampaign;
import com.chutneytesting.execution.api.report.surefire.Testsuite.Testcase;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
import com.chutneytesting.execution.domain.report.ScenarioExecutionReport;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.execution.domain.report.StepExecutionReportCore;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

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

        ScenarioExecutionReport report = new ScenarioExecutionReport(1L, "scenario name", successStepReport);
        ExecutionHistory.Execution execution = ImmutableExecutionHistory.Execution
            .builder()
            .executionId(report.executionId)
            .duration(320L)
            .status(ServerReportStatus.SUCCESS)
            .time(LocalDateTime.now())
            .report(objectMapper.writeValueAsString(report))
            .testCaseTitle("fake")
            .environment("")
            .build();

        ScenarioExecutionReportCampaign scenarioExecutionReportCampaign = new ScenarioExecutionReportCampaign("123", "test1", execution.summary());
        when(executionHistoryRepository.getExecution(scenarioExecutionReportCampaign.scenarioId, report.executionId)).thenReturn(execution);

        // When
        Testsuite testsuite = sut.create(scenarioExecutionReportCampaign);

        // Then
        assertThat(testsuite.getName()).isEqualTo("test1");
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

        ScenarioExecutionReport report = new ScenarioExecutionReport(1L, "scenario name", failureStepReport);
        ExecutionHistory.Execution execution = ImmutableExecutionHistory.Execution
            .builder()
            .executionId(report.executionId)
            .duration(23546L)
            .status(ServerReportStatus.FAILURE)
            .time(LocalDateTime.now())
            .report(objectMapper.writeValueAsString(report))
            .testCaseTitle("fake")
            .environment("")
            .build();

        ScenarioExecutionReportCampaign scenarioExecutionReportCampaign = new ScenarioExecutionReportCampaign("123", "test2", execution.summary());
        when(executionHistoryRepository.getExecution(scenarioExecutionReportCampaign.scenarioId, report.executionId)).thenReturn(execution);

        // When
        Testsuite testsuite = sut.create(scenarioExecutionReportCampaign);

        // Then
        assertThat(testsuite.getName()).isEqualTo("test2");
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

        return new StepExecutionReportCore(0L,
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
            Maps.newHashMap());
    }

}
