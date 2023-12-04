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

package com.chutneytesting.campaign.api.dto;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CampaignDto {

    private Long id;
    private String title;
    private String description;
    private List<String> scenarioIds;
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
