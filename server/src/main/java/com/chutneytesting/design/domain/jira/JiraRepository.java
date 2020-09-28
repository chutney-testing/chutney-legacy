package com.chutneytesting.design.domain.jira;

import com.chutneytesting.admin.domain.Backupable;

public interface JiraRepository extends Backupable {

    String getByScenarioId(String scenarioId);

    void saveForScenario(String scenarioId, String jiraId);

    void removeForScenario(String scenarioId);

    String getByCampaignId(String campaignId);

    void saveForCampaign(String campaignId, String jiraId);

    void removeForCampaign(String campaignId);

    JiraTargetConfiguration loadServerConfiguration();

    void saveServerConfiguration(JiraTargetConfiguration jiraTargetConfiguration);

}
