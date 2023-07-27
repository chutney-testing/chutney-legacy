package com.chutneytesting.server.core.domain.instrument;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import org.springframework.http.HttpStatusCode;

public interface ChutneyMetrics {

    void onScenarioExecutionEnded(TestCase testCase, ExecutionHistory.Execution execution);

    void onCampaignExecutionEnded(Campaign campaign, CampaignExecutionReport campaignExecutionReport);

    void onHttpError(HttpStatusCode status);
}
