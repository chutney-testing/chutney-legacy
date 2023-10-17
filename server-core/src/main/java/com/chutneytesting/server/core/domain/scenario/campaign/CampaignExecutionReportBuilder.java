package com.chutneytesting.server.core.domain.scenario.campaign;

import static java.util.Optional.ofNullable;

import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CampaignExecutionReportBuilder {
    // Mandatory fields
    private Long executionId;
    private String campaignName;
    private boolean partialExecution;
    private String executionEnvironment;
    private String dataSetId;
    private Integer dataSetVersion;
    private String userId;

    // Optional fields
    private List<ScenarioExecutionCampaign> scenarioExecutionReports = new ArrayList<>();
    private Long campaignId;
    private LocalDateTime startDate;
    private ServerReportStatus status;

    public static CampaignExecutionReportBuilder builder() {
        return new CampaignExecutionReportBuilder();
    }

    private CampaignExecutionReportBuilder() {

    }

    public CampaignExecutionReportBuilder setExecutionId(Long executionId) {
        this.executionId = executionId;
        return this;
    }

    public CampaignExecutionReportBuilder setCampaignName(String campaignName) {
        this.campaignName = campaignName;
        return this;
    }

    public CampaignExecutionReportBuilder setPartialExecution(boolean partialExecution) {
        this.partialExecution = partialExecution;
        return this;
    }

    public CampaignExecutionReportBuilder setExecutionEnvironment(String executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
        return this;
    }

    public CampaignExecutionReportBuilder setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public CampaignExecutionReportBuilder setStatus(ServerReportStatus status) {
        this.status = status;
        return this;
    }

    public CampaignExecutionReportBuilder setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
        return this;
    }

    public CampaignExecutionReportBuilder setDataSetVersion(Integer dataSetVersion) {
        this.dataSetVersion = dataSetVersion;
        return this;
    }

    public CampaignExecutionReportBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public CampaignExecutionReportBuilder addScenarioExecutionReport(ScenarioExecutionCampaign scenarioExecutionReport) {
        this.scenarioExecutionReports.add(scenarioExecutionReport);
        return this;
    }

    public CampaignExecutionReportBuilder setScenarioExecutionReport(List<ScenarioExecutionCampaign> scenarioExecutionsReports) {
        this.scenarioExecutionReports = new ArrayList<>(scenarioExecutionsReports);
        return this;
    }

    public CampaignExecutionReportBuilder setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
        return this;
    }

    public CampaignExecution build() {
        return new CampaignExecution(
            executionId,
            campaignId,
            campaignName,
            partialExecution,
            executionEnvironment,
            userId,
            ofNullable(dataSetId),
            ofNullable(dataSetVersion),
            startDate,
            status,
            scenarioExecutionReports
        );
    }
}
