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

package com.chutneytesting.server.core.domain.instrument;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import org.springframework.http.HttpStatusCode;

public interface ChutneyMetrics {

    void onScenarioExecutionEnded(TestCase testCase, ExecutionHistory.Execution execution);

    void onCampaignExecutionEnded(Campaign campaign, CampaignExecution campaignExecution);

    void onHttpError(HttpStatusCode status);
}
