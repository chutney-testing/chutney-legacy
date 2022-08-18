package com.chutneytesting.instrument.domain;

import com.chutneytesting.campaign.domain.Campaign;
import com.chutneytesting.campaign.domain.CampaignExecutionReport;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.scenario.domain.TestCase;
import org.springframework.http.HttpStatus;
public interface ChutneyMetrics {

    void onScenarioExecutionEnded(TestCase testCase, ExecutionHistory.Execution execution);

    void onCampaignExecutionEnded(Campaign campaign, CampaignExecutionReport campaignExecutionReport);

    void onHttpError(HttpStatus status);
}
