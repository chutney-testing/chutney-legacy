package com.chutneytesting.design.api.campaign.dto;

import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CampaignExecutionReportDto {

    private Long executionId;
    private String campaignName;
    private LocalDateTime startDate;
    private ServerReportStatus status;
    private List<ScenarioExecutionReportOutlineDto> scenarioExecutionReports;
    private boolean partialExecution;
    private String executionEnvironment;

    public CampaignExecutionReportDto(@JsonProperty("executionId") Long executionId,
                                      @JsonProperty("scenarioExecutionReports") List<ScenarioExecutionReportOutlineDto> scenarioExecutionReports,
                                      @JsonProperty("campaignName") String campaignName,
                                      @JsonProperty("startDate") LocalDateTime startDate,
                                      @JsonProperty("status") ServerReportStatus status,
                                      @JsonProperty("partialExecution") boolean partialExecution,
                                      @JsonProperty("executionEnvironment") String executionEnvironment) {
        this.executionId = executionId;
        this.scenarioExecutionReports = scenarioExecutionReports;
        this.campaignName = campaignName;
        this.startDate = startDate;
        this.status = status;
        this.partialExecution = partialExecution;
        this.executionEnvironment = executionEnvironment;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public List<ScenarioExecutionReportOutlineDto> getScenarioExecutionReports() {
        return scenarioExecutionReports;
    }

    public long getDuration() {
        Optional<LocalDateTime> latestExecutionEndDate = scenarioExecutionReports.stream()
            .map(report -> report.getStartDate().plus(report.getDuration(), ChronoUnit.MILLIS))
            .max(LocalDateTime::compareTo);

        return latestExecutionEndDate
            .map(endDate -> ChronoUnit.MILLIS.between(startDate, endDate))
            .orElse(0L);
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public ServerReportStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "CampaignExecutionReport{" +
            "executionId=" + executionId +
            '}';
    }

    public String getCampaignName() {
        return campaignName;
    }

    public boolean isPartialExecution() {
        return partialExecution;
    }

    public String getExecutionEnvironment() {
        return executionEnvironment;
    }
}
