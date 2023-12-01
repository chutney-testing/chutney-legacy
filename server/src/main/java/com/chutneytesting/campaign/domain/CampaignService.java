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

import static java.util.stream.Collectors.toList;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.util.List;

public class CampaignService {

    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public CampaignExecution findByExecutionId(Long campaignExecutionId) {
        CampaignExecution report = campaignRepository.findByExecutionId(campaignExecutionId);
        return report.withoutRetries();
    }

    public List<CampaignExecution> findExecutionsById(Long campaignId) {
        return campaignRepository.findExecutionsById(campaignId).stream()
            .map(CampaignExecution::withoutRetries)
            .collect(toList());
    }
}
