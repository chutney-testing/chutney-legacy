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

    JiraTargetConfiguration loadServerConfiguration();

    void saveServerConfiguration(JiraTargetConfiguration jiraTargetConfiguration);
}
