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

package com.chutneytesting.campaign.domain;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CampaignExecutionRepository {
    Optional<CampaignExecution> currentExecution(Long campaignId);

    List<CampaignExecution> currentExecutions();

    void startExecution(Long campaignId, CampaignExecution campaignExecution);

    void stopExecution(Long campaignId);

    CampaignExecution getLastExecution(Long campaignId);

    void deleteExecutions(Set<Long> executionsIds);

    void saveCampaignExecution(Long campaignId, CampaignExecution execution);

    void clearAllExecutionHistory(Long id);

    List<CampaignExecution> getLastExecutions(Long numberOfExecution);

    Long generateCampaignExecutionId(Long campaignId, String environment);

    List<CampaignExecution> getExecutionHistory(Long campaignId);

    CampaignExecution getCampaignExecutionById(Long campaignExecutionId);
}
