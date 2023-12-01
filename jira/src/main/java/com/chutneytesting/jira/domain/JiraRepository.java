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

package com.chutneytesting.jira.domain;

import java.nio.file.Path;
import java.util.Map;

public interface JiraRepository {

    Path getFolderPath();

    Map<String, String> getAllLinkedCampaigns();

    /**
     * @return key chutney id, value jira id
     */
    Map<String, String> getAllLinkedScenarios();

    String getByScenarioId(String scenarioId);

    void saveForScenario(String scenarioId, String jiraId);

    void removeForScenario(String scenarioId);

    String getByCampaignId(String campaignId);

    void saveForCampaign(String campaignId, String jiraId);

    void removeForCampaign(String campaignId);

    String getByCampaignExecutionId(String campaignExecutionId);

    void saveForCampaignExecution(String campaignExecutionId, String jiraId);

    void removeForCampaignExecution(String campaignExecutionId);

    JiraTargetConfiguration loadServerConfiguration();

    void saveServerConfiguration(JiraTargetConfiguration jiraTargetConfiguration);
}
