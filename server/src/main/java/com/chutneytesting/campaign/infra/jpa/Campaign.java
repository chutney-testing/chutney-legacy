package com.chutneytesting.campaign.infra.jpa;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.chutneytesting.scenario.infra.raw.TagListMapper;
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
public class Campaign implements Serializable {

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
    private List<CampaignScenario> campaignScenarios;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "campaign")
    private Set<CampaignParameter> parameters;

    public Campaign() {
    }

    public Campaign(String title) {
        this(null, title, "", null, false, false, null, null, null, null, null);
    }

    public Campaign(String title, List<CampaignScenario> scenarios) {
        this(null, title, "", null, false, false, null, null, null, scenarios, null);
    }

    public Campaign(Long id, String title, String description, String environment, boolean parallelRun, boolean retryAuto, String datasetId, List<String> tags, Integer version, List<CampaignScenario> campaignScenarios, Set<CampaignParameter> parameters) {
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

    public static Campaign fromDomain(com.chutneytesting.server.core.domain.scenario.campaign.Campaign campaign, Integer version) {
        return new Campaign(
            campaign.id,
            campaign.title,
            campaign.description,
            campaign.executionEnvironment(),
            campaign.parallelRun,
            campaign.retryAuto,
            campaign.externalDatasetId,
            campaign.tags,
            version,
            CampaignScenario.fromDomain(campaign),
            CampaignParameter.fromDomain(campaign)
        );
    }

    private void fromCampaignScenarios(List<CampaignScenario> campaignScenarios) {
        initCampaignScenarios();
        if (campaignScenarios != null && !campaignScenarios.isEmpty()) {
            this.campaignScenarios.clear();
            this.campaignScenarios.addAll(campaignScenarios);
            attachCampaignScenarios();
        }
    }

    private void fromCampaignParameters(Set<CampaignParameter> campaignParameters) {
        initParameters();
        if (campaignParameters != null && !campaignParameters.isEmpty()) {
            this.parameters.clear();
            this.parameters.addAll(campaignParameters);
            attachParameters();
        }
    }

    public com.chutneytesting.server.core.domain.scenario.campaign.Campaign toDomain() {
        return new com.chutneytesting.server.core.domain.scenario.campaign.Campaign(
            id,
            title,
            description,
            campaignScenarios.stream().map(CampaignScenario::scenarioId).toList(),
            parameters.stream().collect(toMap(CampaignParameter::parameter, CampaignParameter::value)),
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

    public List<CampaignScenario> campaignScenarios() {
        return campaignScenarios;
    }

    public Set<CampaignParameter> parameters() {
        return parameters;
    }

    public Integer version() {
        return version;
    }

    public void removeScenario(String scenarioId) {
        Optional<CampaignScenario> campaignScenario = campaignScenarios.stream()
            .filter(cs -> cs.scenarioId().equals(scenarioId))
            .findFirst();
        if (campaignScenario.isPresent()) {
            CampaignScenario cs = campaignScenario.get();
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
