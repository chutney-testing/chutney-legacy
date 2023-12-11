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

package com.chutneytesting.campaign.infra;

import static java.util.Optional.ofNullable;
import static org.assertj.core.util.Lists.newArrayList;

import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.assertj.core.util.Lists;

public class FakeCampaignRepository implements CampaignRepository {

    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, Campaign> campaignsById = new HashMap<>();
    private final Map<Long, List<CampaignExecution>> campaignsExecutionById = new HashMap<>();
    private final Multimap<String, Campaign> campaignsByName = ArrayListMultimap.create();

    @Override
    public Campaign createOrUpdate(Campaign campaign) {
        final Campaign saved;
        if (campaign.id != null && campaignsById.containsKey(campaign.id)) {
            saved = campaign;
        } else {
            saved = new Campaign(sequence.incrementAndGet(), campaign.title, campaign.description, campaign.scenarioIds, "env", false, false, null, campaign.tags);
        }
        campaignsById.put(saved.id, saved);
        campaignsByName.put(saved.title, saved);

        return saved;
    }

    @Override
    public void saveExecution(Long campaignId, CampaignExecution execution) {
        ofNullable(campaignsById.get(campaignId)).ifPresent(campaign -> {
            Campaign c = new Campaign(campaign.id, campaign.title, campaign.title, campaign.scenarioIds, campaign.executionEnvironment(), false, false, null, null);
            createOrUpdate(c);
        });

        List<CampaignExecution> foundReport = campaignsExecutionById.get(campaignId);
        if (foundReport == null) {
            foundReport = Lists.newArrayList();
        }
        foundReport.add(execution);
        campaignsExecutionById.put(campaignId, foundReport);
    }

    @Override
    public boolean removeById(Long id) {
        return campaignsById.remove(id) != null;
    }

    @Override
    public Campaign findById(Long campaignId) throws CampaignNotFoundException {
        if (!campaignsById.containsKey(campaignId)) {
            throw new CampaignNotFoundException(campaignId);
        }
        return campaignsById.get(campaignId);
    }

    @Override
    public List<Campaign> findAll() {
        return newArrayList(campaignsById.values());
    }

    @Override
    public List<Campaign> findByName(String campaignName) {
        return newArrayList(campaignsByName.get(campaignName));
    }

    @Override
    public List<CampaignExecution> findExecutionsById(Long campaignId) {
        return ofNullable(campaignsExecutionById.get(campaignId)).orElse(newArrayList());
    }

    @Override
    public List<CampaignExecution> findLastExecutions(Long numberOfExecution) {
        List<CampaignExecution> allExecutions = campaignsExecutionById.entrySet().stream()
            .flatMap(e -> e.getValue().stream())
            .sorted(executionComparatorReportByExecutionId())
            .collect(Collectors.toList());

        if (numberOfExecution < allExecutions.size()) {
            return allExecutions.subList(0, numberOfExecution.intValue());
        } else {
            return allExecutions;
        }
    }

    @Override
    public List<String> findScenariosIds(Long campaignId) {
        return campaignsById.get(campaignId).scenarioIds;
    }

    @Override
    public Long newCampaignExecution(Long campaignId) {
        return new Random(100).nextLong();
    }

    @Override
    public CampaignExecution findByExecutionId(Long campaignExecutionId) {
        throw new NotImplementedException();
    }

    @Override
    public List<Campaign> findCampaignsByScenarioId(String scenarioId) {
        return campaignsById.values().stream()
            .filter(campaign -> campaign.scenarioIds.contains(scenarioId))
            .collect(Collectors.toList());
    }

    // Duplicate of com.chutneytesting.design.api.campaign.CampaignController#executionComparatorReportByExecutionId
    private static Comparator<CampaignExecution> executionComparatorReportByExecutionId() {
        return Comparator.<CampaignExecution>comparingLong(value -> value.executionId).reversed();
    }
}
