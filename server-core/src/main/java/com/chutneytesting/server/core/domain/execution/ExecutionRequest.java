/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
