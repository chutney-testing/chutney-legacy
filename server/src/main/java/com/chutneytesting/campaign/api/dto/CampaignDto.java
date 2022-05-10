package com.chutneytesting.campaign.api.dto;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CampaignDto {

    private Long id;
    private String title;
    private String description;
    private List<String> scenarioIds;
    @JsonProperty("computedParameters")
    private Map<String, String> executionParameters;
    private List<CampaignExecutionReportDto> campaignExecutionReports;
    private String environment;
    private boolean parallelRun;
    private boolean retryAuto;
    private String datasetId;
    private List<String> tags;

    public CampaignDto() {
    }

    public CampaignDto(Long id,
                       String title,
                       String description,
                       List<String> scenarioIds,
                       Map<String, String> executionParameters,
                       List<CampaignExecutionReportDto> campaignExecutionReports,
                       String environment,
                       boolean parallelRun,
                       boolean retryAuto,
                       String datasetId,
                       List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.scenarioIds = scenarioIds;
        this.executionParameters = executionParameters;
        this.campaignExecutionReports = ofNullable(campaignExecutionReports).orElseGet(ArrayList::new);
        this.environment = environment;
        this.parallelRun = parallelRun;
        this.retryAuto = retryAuto;
        this.datasetId = datasetId;
        this.tags = ofNullable(tags).orElseGet(ArrayList::new);
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

    public List<String> getTags() {
        return tags;
    }
}
