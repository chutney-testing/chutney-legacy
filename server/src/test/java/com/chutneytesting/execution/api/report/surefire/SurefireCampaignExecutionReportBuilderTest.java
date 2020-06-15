package com.chutneytesting.execution.api.report.surefire;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.ScenarioExecutionReportCampaign;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
import com.chutneytesting.execution.domain.report.ScenarioExecutionReport;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.execution.domain.report.StepExecutionReportCore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.assertj.core.util.Lists;
import org.junit.Test;

public class SurefireCampaignExecutionReportBuilderTest {

    private ExecutionHistoryRepository executionHistoryRepository = mock(ExecutionHistoryRepository.class);
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private SurefireScenarioExecutionReportBuilder surefireScenarioExecutionReportBuilder = new SurefireScenarioExecutionReportBuilder(objectMapper, executionHistoryRepository);
    private final SurefireCampaignExecutionReportBuilder surefireCampaignExecutionReportBuilder = new SurefireCampaignExecutionReportBuilder(surefireScenarioExecutionReportBuilder);

    @Test
    public void zip_report_OK() throws IOException {
        // Given a success scenario execution
        StepExecutionReportCore successStepReport =
            stepReport("root step Title", -1L, ServerReportStatus.SUCCESS,
                stepReport("step 1", 24L, ServerReportStatus.SUCCESS,
                    stepReport("step1.1", 23L, ServerReportStatus.SUCCESS)));

        ScenarioExecutionReport success_report = new ScenarioExecutionReport(1L, "scenario name", "", "",successStepReport);
        ExecutionHistory.Execution success_execution = ImmutableExecutionHistory.Execution
            .builder()
            .executionId(success_report.executionId)
            .duration(320L)
            .status(ServerReportStatus.SUCCESS)
            .time(LocalDateTime.now())
            .report(objectMapper.writeValueAsString(success_report))
            .testCaseTitle("fake")
            .environment("")
            .user("")
            .build();

        ScenarioExecutionReportCampaign scenarioExecutionReportOK = new ScenarioExecutionReportCampaign("123", "test ♥ Scenario Title ok", success_execution.summary());
        when(executionHistoryRepository.getExecution(scenarioExecutionReportOK.scenarioId, success_report.executionId)).thenReturn(success_execution);

        // And a failure scenario execution
        StepExecutionReportCore failureStepReport =
            stepReport("root step Title", -1L, ServerReportStatus.FAILURE,
                stepReport("step1", 23L, ServerReportStatus.SUCCESS),
                stepReport("step2", 420L, ServerReportStatus.FAILURE),
                stepReport("step3", 0L, ServerReportStatus.NOT_EXECUTED));

        ScenarioExecutionReport failure_report = new ScenarioExecutionReport(2L, "scenario name", "", "",failureStepReport);
        ExecutionHistory.Execution failure_execution = ImmutableExecutionHistory.Execution
            .builder()
            .executionId(failure_report.executionId)
            .duration(23546L)
            .status(ServerReportStatus.FAILURE)
            .time(LocalDateTime.now())
            .report(objectMapper.writeValueAsString(failure_report))
            .testCaseTitle("fake")
            .environment("")
            .user("")
            .build();

        ScenarioExecutionReportCampaign scenarioExecutionReportKO = new ScenarioExecutionReportCampaign("123", "test Scenario Title ko", failure_execution.summary());
        when(executionHistoryRepository.getExecution(scenarioExecutionReportKO.scenarioId, failure_report.executionId)).thenReturn(failure_execution);

        // And a campaign report with previous scenario executions
        CampaignExecutionReport campaignExecutionReport1 = new CampaignExecutionReport(1L, 1L, Arrays.asList(scenarioExecutionReportOK, scenarioExecutionReportKO), "test Campaign Title", false, "", null, null, "");
        CampaignExecutionReport campaignExecutionReport2 = new CampaignExecutionReport(1L, 1L, Arrays.asList(scenarioExecutionReportOK, scenarioExecutionReportKO), "test Campaign Title 2", false, "", null, null, "");

        // When we zip it
        byte[] zip = surefireCampaignExecutionReportBuilder.createReport(Lists.list(campaignExecutionReport1, campaignExecutionReport2));

        // Then it produces a zip with correct content
        ByteArrayInputStream bais = new ByteArrayInputStream(zip);

        List<String> directories = new ArrayList<>();
        Map<String, String> files = new HashMap<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(bais)) {
            ZipEntry nextEntry;
            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                if (nextEntry.isDirectory()) {
                    directories.add(nextEntry.getName());
                } else {
                    files.put(nextEntry.getName(), readContent(zipInputStream));
                }
            }
            assertThat(directories).containsExactlyInAnyOrder("test Campaign Title/", "test Campaign Title 2/");
            assertThat(files).hasSize(4).containsKeys("test Campaign Title/test ♥ Scenario Title ok.xml", "test Campaign Title/test Scenario Title ko.xml", "test Campaign Title 2/test Scenario Title ko.xml", "test Campaign Title 2/test ♥ Scenario Title ok.xml");

            // assert That XML have been serialized:
            assertThat(files.values()).allSatisfy(s -> {
                assertThat(s).startsWith("<?xml version=\"1.0\" ?><testsuite");
            });
        }
    }

    private String readContent(ZipInputStream zis) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        try {
            while ((len = zis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new String(baos.toByteArray());
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
