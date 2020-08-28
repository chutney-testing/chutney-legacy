package com.chutneytesting.design.domain.jira;

import com.chutneytesting.admin.domain.Backupable;

public interface JiraRepository extends Backupable {

    String getByScenarioId(String scenarioId);

    void saveForScenario(String scenarioId, String jiraId);

    String getByCampaignId(String campaignId);

    void saveForCampaign(String campaignId, String jiraId);
}
