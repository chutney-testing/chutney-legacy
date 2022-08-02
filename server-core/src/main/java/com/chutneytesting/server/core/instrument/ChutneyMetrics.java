package com.chutneytesting.server.core.instrument;

import com.chutneytesting.server.core.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.scenario.TestCase;
import com.chutneytesting.server.core.scenario.campaign.Campaign;
import com.chutneytesting.server.core.scenario.campaign.CampaignExecutionReport;

public interface ChutneyMetrics {

    void onScenarioExecutionEnded(TestCase testCase, ExecutionHistory.Execution execution);

    void onCampaignExecutionEnded(Campaign campaign, CampaignExecutionReport campaignExecutionReport);

    void onHttpError(HttpStatus status);
}
