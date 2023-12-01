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

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.chutneytesting.scenario.infra.raw.TagListMapper;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity(name = "CAMPAIGN")
public class CampaignEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ENVIRONMENT")
    private String environment;

    @Column(name = "PARALLEL_RUN")
    private Boolean parallelRun;

    @Column(name = "RETRY_AUTO")
    private Boolean retryAuto;

    @Column(name = "DATASET_ID")
    private String datasetId;

    @Column(name = "TAGS")
    private String tags;

    @Column(name = "VERSION")
    @Version
    private Integer version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "campaign")
    @OrderBy("rank ASC")
    private List<CampaignScenarioEntity> campaignScenarios;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "campaign")
    private Set<CampaignParameterEntity> parameters;

    public CampaignEntity() {
    }

    public CampaignEntity(String title) {
        this(null, title, "", null, false, false, null, null, null, null, null);
    }

    public CampaignEntity(String title, List<CampaignScenarioEntity> scenarios) {
        this(null, title, "", null, false, false, null, null, null, scenarios, null);
    }

    public CampaignEntity(Long id, String title, String description, String environment, boolean parallelRun, boolean retryAuto, String datasetId, List<String> tags, Integer version, List<CampaignScenarioEntity> campaignScenarios, Set<CampaignParameterEntity> parameters) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.environment = environment;
        this.parallelRun = parallelRun;
        this.retryAuto = retryAuto;
        this.datasetId = datasetId;
        this.tags = TagListMapper.tagsListToString(tags);
        this.version = ofNullable(version).orElse(1);
        fromCampaignScenarios(campaignScenarios);
        fromCampaignParameters(parameters);
    }

    public static CampaignEntity fromDomain(Campaign campaign, Integer version) {
        return new CampaignEntity(
            campaign.id,
            campaign.title,
            campaign.description,
            campaign.executionEnvironment(),
            campaign.parallelRun,
            campaign.retryAuto,
            campaign.externalDatasetId,
            campaign.tags,
            version,
            CampaignScenarioEntity.fromDomain(campaign),
            CampaignParameterEntity.fromDomain(campaign)
        );
    }

    private void fromCampaignScenarios(List<CampaignScenarioEntity> campaignScenarioEntities) {
        initCampaignScenarios();
        if (campaignScenarioEntities != null && !campaignScenarioEntities.isEmpty()) {
            this.campaignScenarios.clear();
            this.campaignScenarios.addAll(campaignScenarioEntities);
            attachCampaignScenarios();
        }
    }

    private void fromCampaignParameters(Set<CampaignParameterEntity> campaignParameterEntities) {
        initParameters();
        if (campaignParameterEntities != null && !campaignParameterEntities.isEmpty()) {
            this.parameters.clear();
            this.parameters.addAll(campaignParameterEntities);
            attachParameters();
        }
    }

    public Campaign toDomain() {
        return new Campaign(
            id,
            title,
            description,
            campaignScenarios.stream().map(CampaignScenarioEntity::scenarioId).toList(),
            parameters.stream().collect(toMap(CampaignParameterEntity::parameter, CampaignParameterEntity::value)),
            environment,
            parallelRun,
            retryAuto,
            datasetId,
            TagListMapper.tagsStringToList(tags)
        );
    }

    public Long id() {
        return id;
    }

    public String title() {
        return title;
    }

    public List<CampaignScenarioEntity> campaignScenarios() {
        return campaignScenarios;
    }

    public Set<CampaignParameterEntity> parameters() {
        return parameters;
    }

    public Integer version() {
        return version;
    }

    public void removeScenario(String scenarioId) {
        Optional<CampaignScenarioEntity> campaignScenario = campaignScenarios.stream()
            .filter(cs -> cs.scenarioId().equals(scenarioId))
            .findFirst();
        if (campaignScenario.isPresent()) {
            CampaignScenarioEntity cs = campaignScenario.get();
            campaignScenarios.remove(cs.rank().intValue());
            for (int i = cs.rank(); i < campaignScenarios.size(); i++) {
                campaignScenarios.get(i).rank(i);
            }
        }
    }

    private void initCampaignScenarios() {
        if (this.campaignScenarios == null) {
            this.campaignScenarios = new ArrayList<>();
        }
    }

    private void attachCampaignScenarios() {
        ofNullable(campaignScenarios).ifPresent(css -> css.forEach(cs -> cs.forCampaign(this)));
    }

    private void initParameters() {
        if (this.parameters == null) {
            this.parameters = new HashSet<>();
        }
    }

    private void attachParameters() {
        ofNullable(parameters).ifPresent(params -> params.forEach(param -> param.forCampaign(this)));
    }
}
