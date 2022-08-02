package com.chutneytesting.instrument;

import com.chutneytesting.execution.history.ExecutionHistory;
import com.chutneytesting.scenario.TestCase;
import com.chutneytesting.scenario.campaign.Campaign;
import com.chutneytesting.scenario.campaign.CampaignExecutionReport;

public interface ChutneyMetrics {

    void onScenarioExecutionEnded(TestCase testCase, ExecutionHistory.Execution execution);

    void onCampaignExecutionEnded(Campaign campaign, CampaignExecutionReport campaignExecutionReport);
}
