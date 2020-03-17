package com.chutneytesting.design.domain.campaign;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CampaignExecutionReport {

    // Mandatory
    public final Long executionId;
    public final String campaignName;
    public final boolean partialExecution;
    public final String executionEnvironment;

    // Not mandatory
    public final LocalDateTime startDate;
    private ServerReportStatus status;
    private final List<ScenarioExecutionReportCampaign> scenarioExecutionReports;
    public final Long campaignId;

    public CampaignExecutionReport(Long executionId, String campaignName, boolean partialExecution, String executionEnvironment) {
        this.executionId = executionId;
        this.campaignId = null;
        this.partialExecution = partialExecution;
        this.executionEnvironment = executionEnvironment;
        this.scenarioExecutionReports = new ArrayList<>();
        this.campaignName = campaignName;
        this.startDate = LocalDateTime.now();
        this.status = ServerReportStatus.RUNNING;
    }

    public CampaignExecutionReport(Long executionId, Long campaignId, List<ScenarioExecutionReportCampaign> scenarioExecutionReports, String campaignName, boolean partialExecution, String executionEnvironment) {
        this.executionId = executionId;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.scenarioExecutionReports = Collections.unmodifiableList(scenarioExecutionReports);
        this.startDate = findStartDate(scenarioExecutionReports);
        this.status = findStatus(scenarioExecutionReports);
        this.partialExecution = partialExecution;
        this.executionEnvironment = executionEnvironment;
    }

    public void initExecution(List<TestCase> testCases, String executionEnvironment) {
        testCases.stream().forEach(testCase -> {
            this.scenarioExecutionReports.add(
                new ScenarioExecutionReportCampaign(
                    testCase.id(),
                    testCase.metadata().title(),
                    ImmutableExecutionHistory.ExecutionSummary.builder()
                        .executionId(-1L)
                        .testCaseTitle(testCase.metadata().title())
                        .time(LocalDateTime.now())
                        .status(ServerReportStatus.NOT_EXECUTED)
                        .duration(0)
                        .environment(executionEnvironment)
                        .build()));
        });
    }

    public void startScenarioExecution(TestCase testCase, String executionEnvironment) throws UnsupportedOperationException {
        OptionalInt indexOpt = IntStream.range(0, this.scenarioExecutionReports.size())
            .filter(i -> this.scenarioExecutionReports.get(i).scenarioId.equals(testCase.id()))
            .findFirst();
        this.scenarioExecutionReports.set(indexOpt.getAsInt(),
            new ScenarioExecutionReportCampaign(
                testCase.id(),
                testCase.metadata().title(),
                ImmutableExecutionHistory.ExecutionSummary.builder()
                    .executionId(-1L)
                    .testCaseTitle(testCase.metadata().title())
                    .time(LocalDateTime.now())
                    .status(ServerReportStatus.RUNNING)
                    .duration(0)
                    .environment(executionEnvironment)
                    .build()));
    }

    public void endScenarioExecution(ScenarioExecutionReportCampaign scenarioExecutionReportCampaign) throws UnsupportedOperationException {
        int index = this.scenarioExecutionReports.indexOf(scenarioExecutionReportCampaign);
        this.scenarioExecutionReports.set(index, scenarioExecutionReportCampaign);
    }

    public void stopCampaignExecution() {
        this.status = ServerReportStatus.STOPPED;
    }

    public void endCampaignExecution() {
        if (!this.status.isFinal()) {
            this.status = findStatus(this.scenarioExecutionReports);
        }
    }

    public List<ScenarioExecutionReportCampaign> scenarioExecutionReports() {
        return Collections.unmodifiableList(scenarioExecutionReports);
    }

    public ServerReportStatus status() {
        return status;
    }

    private LocalDateTime findStartDate(List<ScenarioExecutionReportCampaign> scenarioExecutionReports) {
        return scenarioExecutionReports.stream()
            .filter(Objects::nonNull)
            .map(report -> report.execution)
            .filter(Objects::nonNull)
            .map(ExecutionHistory.ExecutionProperties::time)
            .filter(Objects::nonNull)
            .reduce((time1, time2) -> {
                if (time1.isBefore(time2)) {
                    return time1;
                } else {
                    return time2;
                }
            })
            .orElse(LocalDateTime.MIN);
    }

    private ServerReportStatus findStatus(List<ScenarioExecutionReportCampaign> scenarioExecutionReports) {
        return scenarioExecutionReports.stream()
            .filter(Objects::nonNull)
            .map(report -> report.execution)
            .filter(Objects::nonNull)
            .map(ExecutionHistory.ExecutionProperties::status)
            .collect(Collectors.collectingAndThen(Collectors.toList(), ServerReportStatus::worst));
    }

    @Override
    public String toString() {
        return "CampaignExecutionReport{" +
            "executionId=" + executionId +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CampaignExecutionReport that = (CampaignExecutionReport) o;
        return Objects.equals(executionId, that.executionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId);
    }


}
