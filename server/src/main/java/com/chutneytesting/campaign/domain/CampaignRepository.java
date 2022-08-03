package com.chutneytesting.campaign.domain;

import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.server.core.scenario.campaign.Campaign;
import com.chutneytesting.server.core.scenario.campaign.CampaignExecutionReport;
import java.util.List;

/**
 * Right-side port for secondary actors of the business domain. See {@link CampaignExecutionEngine}
 *
 * Use to Store Campaign
 */
public interface CampaignRepository {

    Campaign createOrUpdate(Campaign campaign);

    void saveReport(Long campaignId, CampaignExecutionReport report);

    boolean removeById(Long id);

    Campaign findById(Long campaignId) throws CampaignNotFoundException;

    List<Campaign> findAll();

    List<Campaign> findByName(String campaignName);

    List<CampaignExecutionReport> findExecutionsById(Long campaignId);

    List<CampaignExecutionReport> findLastExecutions(Long numberOfExecution);

    List<String> findScenariosIds(Long campaignId);

    List<Campaign> findCampaignsByScenarioId(String scenarioId);

    Long newCampaignExecution();

    CampaignExecutionReport findByExecutionId(Long campaignExecutionId);
}
