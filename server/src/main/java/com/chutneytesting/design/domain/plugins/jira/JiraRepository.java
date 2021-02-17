package com.chutneytesting.design.domain.plugins.jira;

import com.chutneytesting.admin.domain.Backupable;
import java.util.Map;

public interface JiraRepository extends Backupable {

    Map<String, String> getAllLinkedCampaigns();

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
