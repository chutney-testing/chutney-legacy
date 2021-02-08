package com.chutneytesting.design.api.campaign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CampaignDto {

    private final Long id;
    private final String title;
    private final String description;
    private final List<String> scenarioIds;
    private final Map<String, String> executionParameters;
    private final List<CampaignExecutionReportDto> campaignExecutionReports;

    private final String scheduleTime;
    private final String environment;
    private final boolean parallelRun;
    private final boolean retryAuto;
    private final String datasetId;

    public CampaignDto(@JsonProperty("id") Long id,
                       @JsonProperty("title") String title,
                       @JsonProperty("description") String description,
                       @JsonProperty("scenarioIds") List<String> scenarioIds,
                       @JsonProperty("computedParameters") Map<String, String> executionParameters,
                       @JsonProperty("campaignExecutionReports") List<CampaignExecutionReportDto> campaignExecutionReports,
                       @JsonProperty("scheduleTime") String scheduleTime,
                       @JsonProperty("environment") String environment,
                       @JsonProperty("parallelRun") boolean parallelRun,
                       @JsonProperty("retryAuto") boolean retryAuto,
                       @JsonProperty("datasetId") String datasetId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.scenarioIds = scenarioIds;
        this.executionParameters = executionParameters;
        this.campaignExecutionReports = Optional.ofNullable(campaignExecutionReports).orElseGet(ArrayList::new);
        this.scheduleTime = scheduleTime;
        this.environment = environment;
        this.parallelRun = parallelRun;
        this.retryAuto = retryAuto;
        this.datasetId = datasetId;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getScenarioIds() {
        return scenarioIds;
    }

    public Map<String, String> getExecutionParameters() {
        return executionParameters;
    }

    public List<CampaignExecutionReportDto> getCampaignExecutionReports() {
        return campaignExecutionReports;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public Optional<String> safeGetScheduleTime() {
        if (scheduleTime == null || scheduleTime.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(scheduleTime);
    }

    public String getEnvironment() {
        return environment;
    }

    public boolean isParallelRun() {
        return parallelRun;
    }

    public boolean isRetryAuto() {
        return retryAuto;
    }

    public String getDatasetId() {
        return datasetId;
    }
}
