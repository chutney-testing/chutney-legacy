package com.chutneytesting.campaign.domain;

import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.util.List;

/**
 * Right-side port for secondary actors of the business domain. See {@link CampaignExecutionEngine}
 *
 * Use to Store Campaign
 */
public interface CampaignRepository {

    Campaign createOrUpdate(Campaign campaign);

    void saveExecution(Long campaignId, CampaignExecution execution);

    boolean removeById(Long id);

    Campaign findById(Long campaignId) throws CampaignNotFoundException;

    List<Campaign> findAll();

    List<Campaign> findByName(String campaignName);

    List<CampaignExecution> findExecutionsById(Long campaignId);

    List<CampaignExecution> findLastExecutions(Long numberOfExecution);

    List<String> findScenariosIds(Long campaignId);

    List<Campaign> findCampaignsByScenarioId(String scenarioId);

    Long newCampaignExecution(Long campaignId);

    CampaignExecution findByExecutionId(Long campaignExecutionId);
}
