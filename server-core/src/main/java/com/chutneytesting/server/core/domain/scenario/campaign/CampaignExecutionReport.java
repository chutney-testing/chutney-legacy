package com.chutneytesting.server.core.domain.scenario.campaign;

import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.RUNNING;
import static java.time.LocalDateTime.now;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CampaignExecutionReport {

    // Mandatory
    public final Long executionId;
    public final String campaignName;
    public final boolean partialExecution;
    public final String executionEnvironment;
    public final Optional<String> dataSetId;
    public final Optional<Integer> dataSetVersion;
    public final String userId;

    // Not mandatory
    public final LocalDateTime startDate;
    private ServerReportStatus status;
    private final List<ScenarioExecutionReportCampaign> scenarioExecutionReports;
    public final Long campaignId;

    public CampaignExecutionReport(Long executionId,
                                   String campaignName,
                                   boolean partialExecution,
                                   String executionEnvironment,
                                   String dataSetId,
                                   Integer dataSetVersion,
                                   String userId) {
        this.executionId = executionId;
        this.campaignId = null;
        this.partialExecution = partialExecution;
        this.executionEnvironment = executionEnvironment;
        this.scenarioExecutionReports = new ArrayList<>();
        this.campaignName = campaignName;
        this.startDate = now();
        this.status = RUNNING;
        this.dataSetId = ofNullable(dataSetId);
        this.dataSetVersion = ofNullable(dataSetVersion);
        this.userId = userId;
    }

    public CampaignExecutionReport(Long executionId,
                                   Long campaignId,
                                   List<ScenarioExecutionReportCampaign> scenarioExecutionReports,
                                   String campaignName,
                                   boolean partialExecution,
                                   String executionEnvironment,
                                   String dataSetId,
                                   Integer dataSetVersion,
                                   String userId) {
        this.executionId = executionId;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.scenarioExecutionReports = scenarioExecutionReports;
        this.startDate = findStartDate(scenarioExecutionReports);
        this.status = findStatus(scenarioExecutionReports);
        this.partialExecution = partialExecution;
        this.executionEnvironment = executionEnvironment;
        this.dataSetId = ofNullable(dataSetId);
        this.dataSetVersion = ofNullable(dataSetVersion);
        this.userId = userId;
    }

    CampaignExecutionReport(
        Long executionId,
        Long campaignId,
        String campaignName,
        boolean partialExecution,
        String executionEnvironment,
        String userId,
        Optional<String> dataSetId,
        Optional<Integer> dataSetVersion,
        LocalDateTime startDate,
        ServerReportStatus status,
        List<ScenarioExecutionReportCampaign> scenarioExecutionReports
    ) {
        this.executionId = executionId;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.partialExecution = partialExecution;
        this.executionEnvironment = executionEnvironment;
        this.dataSetId = dataSetId;
        this.dataSetVersion = dataSetVersion;
        this.userId = userId;

        if (scenarioExecutionReports == null) {
            this.startDate = ofNullable(startDate).orElse(now());
            this.status = ofNullable(status).orElse(RUNNING);
            this.scenarioExecutionReports = null;
        } else {
            this.startDate = findStartDate(scenarioExecutionReports);
            this.status = findStatus(scenarioExecutionReports);
            this.scenarioExecutionReports = scenarioExecutionReports;
        }
    }

    public void initExecution(List<TestCase> testCases, String executionEnvironment, String userId) {
        testCases.forEach(testCase ->
            this.scenarioExecutionReports.add(
                new ScenarioExecutionReportCampaign(
                    testCase.id(),
                    testCase.metadata().title(),
                    ImmutableExecutionHistory.ExecutionSummary.builder()
                        .executionId(-1L)
                        .testCaseTitle(testCase.metadata().title())
                        .time(now())
                        .status(ServerReportStatus.NOT_EXECUTED)
                        .duration(0)
                        .environment(executionEnvironment)
                        .datasetId(dataSetId)
                        .datasetVersion(dataSetVersion)
                        .user(userId)
                        .build())));
    }

    public void startScenarioExecution(TestCase testCase, String executionEnvironment, String userId) throws UnsupportedOperationException {
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
                    .time(now())
                    .status(RUNNING)
                    .duration(0)
                    .environment(executionEnvironment)
                    .datasetId(dataSetId)
                    .datasetVersion(dataSetVersion)
                    .user(userId)
                    .build()));
    }

    public void endScenarioExecution(ScenarioExecutionReportCampaign scenarioExecutionReportCampaign) throws UnsupportedOperationException {
        int index = this.scenarioExecutionReports.indexOf(scenarioExecutionReportCampaign);
        this.scenarioExecutionReports.set(index, scenarioExecutionReportCampaign);
    }

    public void endCampaignExecution() {
        if (!this.status.isFinal()) {
            this.status = findStatus(this.scenarioExecutionReports);
        }
    }

    public List<ScenarioExecutionReportCampaign> scenarioExecutionReports() {
        if (findStatus(scenarioExecutionReports).isFinal()) {
            scenarioExecutionReports.sort(ScenarioExecutionReportCampaign.executionIdComparator());
        }
        return unmodifiableList(scenarioExecutionReports);
    }

    public ServerReportStatus status() {
        return status;
    }

    public long getDuration() {
        Optional<LocalDateTime> latestExecutionEndDate = scenarioExecutionReports.stream()
            .map(report -> report.execution.time().plus(report.execution.duration(), ChronoUnit.MILLIS))
            .max(LocalDateTime::compareTo);

        return latestExecutionEndDate
            .map(endDate -> ChronoUnit.MILLIS.between(startDate, endDate))
            .orElse(0L);
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

        List<ScenarioExecutionReportCampaign> filteredReports = filterRetry(scenarioExecutionReports);

        ServerReportStatus foundStatus = filteredReports.stream()
            .map(report -> report.execution)
            .filter(Objects::nonNull)
            .map(ExecutionHistory.ExecutionProperties::status)
            .collect(Collectors.collectingAndThen(Collectors.toList(), ServerReportStatus::worst));
        if (foundStatus.equals(ServerReportStatus.NOT_EXECUTED)) {
            return ServerReportStatus.STOPPED;
        }
        return foundStatus;
    }

    private List<ScenarioExecutionReportCampaign> filterRetry(List<ScenarioExecutionReportCampaign> scenarioExecutionReports) {
        return scenarioExecutionReports.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(s -> s.scenarioId))
            .values().stream()
            .map(list -> list.size() == 1 ? list.get(0) : list.stream().max(Comparator.comparing(objet -> objet.execution.time())).get())
            .toList();
    }

    public CampaignExecutionReport withoutRetries() {
        return CampaignExecutionReportBuilder.builder()
            .setExecutionId(executionId)
            .setCampaignId(campaignId)
            .setPartialExecution(partialExecution)
            .setCampaignName(campaignName)
            .setExecutionEnvironment(executionEnvironment)
            .setDataSetId(dataSetId.orElse(null))
            .setDataSetVersion(dataSetVersion.orElse(null))
            .setUserId(userId)
            .setStartDate(startDate)
            .setStatus(status)
            .setScenarioExecutionReport(filterRetry(scenarioExecutionReports))
            .build();
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
