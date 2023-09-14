package com.chutneytesting.server.core.domain.scenario.campaign;

import java.util.List;
import java.util.Map;

public class CampaignBuilder {
    private Long id;
    private String title;
    private String description;
    private List<String> scenarioIds;
    private Map<String, String> executionParameters;
    private String environment;
    private boolean parallelRun;
    private boolean retryAuto;
    private String externalDatasetId;
    private List<String> tags;

    public static CampaignBuilder builder() {
        return new CampaignBuilder();
    }

    private CampaignBuilder(){

    }
    public CampaignBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public CampaignBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public CampaignBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public CampaignBuilder setScenarioIds(List<String> scenarioIds) {
        this.scenarioIds = scenarioIds;
        return this;
    }

    public CampaignBuilder setExecutionParameters(Map<String, String> executionParameters) {
        this.executionParameters = executionParameters;
        return this;
    }

    public CampaignBuilder setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public CampaignBuilder setParallelRun(boolean parallelRun) {
        this.parallelRun = parallelRun;
        return this;
    }

    public CampaignBuilder setRetryAuto(boolean retryAuto) {
        this.retryAuto = retryAuto;
        return this;
    }

    public CampaignBuilder setExternalDatasetId(String externalDatasetId) {
        this.externalDatasetId = externalDatasetId;
        return this;
    }

    public CampaignBuilder setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public Campaign build() {
        return new Campaign(id, title, description, scenarioIds, executionParameters, environment, parallelRun, retryAuto, externalDatasetId, tags);
    }
}
