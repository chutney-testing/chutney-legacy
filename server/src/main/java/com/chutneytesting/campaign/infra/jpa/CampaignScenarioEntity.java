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

package com.chutneytesting.campaign.infra.jpa;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.List;
import java.util.stream.IntStream;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity(name = "CAMPAIGN_SCENARIOS")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CampaignScenarioEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAMPAIGN_ID")
    private CampaignEntity campaign;

    @Column(name = "SCENARIO_ID")
    private String scenarioId;

    @Column(name = "RANK")
    private Integer rank;

    public CampaignScenarioEntity() {
    }

    public CampaignScenarioEntity(String scenarioId, Integer rank) {
        this(null, scenarioId, rank);
    }

    public CampaignScenarioEntity(CampaignEntity campaign, String scenarioId, Integer rank) {
        this.campaign = campaign;
        this.scenarioId = scenarioId;
        this.rank = rank;
    }

    public String scenarioId() {
        return scenarioId;
    }

    public CampaignEntity campaign() {
        return campaign;
    }

    public Integer rank() {
        return rank;
    }

    public void forCampaign(CampaignEntity campaign) {
        this.campaign = campaign;
    }

    public static List<CampaignScenarioEntity> fromDomain(com.chutneytesting.server.core.domain.scenario.campaign.Campaign campaign) {
        return IntStream.range(0, campaign.scenarioIds.size())
            .mapToObj(idx -> new CampaignScenarioEntity(campaign.scenarioIds.get(idx), idx))
            .toList();
    }

    public void rank(Integer rank) {
        this.rank = rank;
    }
}
