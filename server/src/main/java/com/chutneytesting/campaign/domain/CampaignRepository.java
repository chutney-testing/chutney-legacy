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

import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import java.util.List;

/**
 * Right-side port for secondary actors of the business domain. See {@link CampaignExecutionEngine}
 *
 * Use to Store Campaign
 */
public interface CampaignRepository {

    Campaign createOrUpdate(Campaign campaign);

    boolean removeById(Long id);

    Campaign findById(Long campaignId) throws CampaignNotFoundException;

    List<Campaign> findAll();

    List<Campaign> findByName(String campaignName);

    List<String> findScenariosIds(Long campaignId);

    List<Campaign> findCampaignsByScenarioId(String scenarioId);

}
