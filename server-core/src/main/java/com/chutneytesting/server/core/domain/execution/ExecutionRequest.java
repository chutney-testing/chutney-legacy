package com.chutneytesting.server.core.domain.execution;

import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;

public class ExecutionRequest {

    public final TestCase testCase;
    public final String environment;
    public final String userId;
    public final DataSet dataset;
    public final CampaignExecution campaignExecution;

    public ExecutionRequest(TestCase testCase, String environment, String userId, DataSet dataset, CampaignExecution campaignExecution) {
        this.testCase = testCase;
        this.environment = environment;
        this.userId = userId;
        this.dataset = dataset;
        this.campaignExecution = campaignExecution;
    }

    public ExecutionRequest(TestCase testCase, String environment, String userId, DataSet dataset) {
        this(testCase, environment, userId, dataset, null);
    }

    public ExecutionRequest(TestCase testCase, String environment, String userId) {
        this(testCase, environment, userId, DataSet.NO_DATASET, null);
    }

}
