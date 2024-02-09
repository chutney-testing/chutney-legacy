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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.campaign.domain.PeriodicScheduledCampaignRepository;
import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.campaign.infra.jpa.CampaignScenarioEntity;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Campaign persistence management.
 */
@Repository
@Transactional
public class DatabaseCampaignRepository implements CampaignRepository {

    private final CampaignJpaRepository campaignJpaRepository;
    private final CampaignScenarioJpaRepository campaignScenarioJpaRepository;
    private final CampaignExecutionRepository campaignExecutionRepository;
    private final PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository;

    public DatabaseCampaignRepository(CampaignJpaRepository campaignJpaRepository,
                                      CampaignScenarioJpaRepository campaignScenarioJpaRepository,
                                      CampaignExecutionDBRepository campaignExecutionRepository, PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository) {
        this.campaignJpaRepository = campaignJpaRepository;
        this.campaignScenarioJpaRepository = campaignScenarioJpaRepository;
        this.campaignExecutionRepository = campaignExecutionRepository;
        this.periodicScheduledCampaignRepository = periodicScheduledCampaignRepository;
    }

    @Override
    public Campaign createOrUpdate(Campaign campaign) {
        if (campaign.id != null && !campaignExists(campaign.id)) {
            CampaignEntity campaignEntity = CampaignEntity.fromDomain(campaign, 0);
            campaignJpaRepository.saveWithExplicitId(campaignEntity.id(), campaignEntity.title(), campaignEntity.description());
        }
        CampaignEntity campaignJpa =
            campaignJpaRepository.save(CampaignEntity.fromDomain(campaign, lastCampaignVersion(campaign.id)));
        return campaignJpa.toDomain();
    }

    private boolean campaignExists(Long id) {
        try {
            findById(id);
            return true;
        } catch (CampaignNotFoundException e) {
           return false;
        }
    }

    private Integer lastCampaignVersion(Long id) {
        return ofNullable(id).flatMap(campaignJpaRepository::findById).map(CampaignEntity::version).orElse(null);
    }

    @Override
    public boolean removeById(Long id) {
        if (campaignJpaRepository.existsById(id)) {
            campaignExecutionRepository.clearAllExecutionHistory(id);
            campaignJpaRepository.deleteById(id);
            periodicScheduledCampaignRepository.removeCampaignId(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Campaign findById(Long campaignId) throws CampaignNotFoundException {
        return campaignJpaRepository.findById(campaignId)
            .map(CampaignEntity::toDomain)
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> findByName(String campaignName) {
        return campaignJpaRepository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), campaignName.toLowerCase()))
            .stream()
            .map(CampaignEntity::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findScenariosIds(Long campaignId) {
        return campaignJpaRepository.findById(campaignId)
            .map(c -> c.campaignScenarios().stream()
                .map(CampaignScenarioEntity::scenarioId)
                .toList()
            )
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> findAll() {
        return StreamSupport.stream(campaignJpaRepository.findAll().spliterator(), false)
            .map(CampaignEntity::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> findCampaignsByScenarioId(String scenarioId) {
        if (isNullOrEmpty(scenarioId)) {
            return emptyList();
        }

        return campaignScenarioJpaRepository.findAllByScenarioId(scenarioId).stream()
            .map(CampaignScenarioEntity::campaign)
            .map(CampaignEntity::toDomain)
            .toList();
    }
}
