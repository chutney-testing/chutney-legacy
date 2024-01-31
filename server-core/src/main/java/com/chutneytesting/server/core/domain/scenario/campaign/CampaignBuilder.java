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

package com.chutneytesting.server.core.domain.scenario.campaign;

import java.util.List;

public class CampaignBuilder {
    private Long id;
    private String title;
    private String description;
    private List<String> scenarioIds;
    private String environment;
    private boolean parallelRun;
    private boolean retryAuto;
    private String externalDatasetId;
    private List<String> tags;

    public static CampaignBuilder builder() {
        return new CampaignBuilder();
    }

    public CampaignBuilder(){

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

    public CampaignBuilder from(Campaign campaign) {
        this.id = campaign.id;
        this.title = campaign.title;
        this.description = campaign.description;
        this.scenarioIds = campaign.scenarioIds;
        this.environment = campaign.executionEnvironment();
        this.parallelRun = campaign.parallelRun;
        this.retryAuto = campaign.retryAuto;
        this.externalDatasetId = campaign.externalDatasetId;
        this.tags = campaign.tags;

        return this;
    }

    public Campaign build() {
        return new Campaign(id, title, description, scenarioIds, environment, parallelRun, retryAuto, externalDatasetId, tags);
    }
}
