package com.chutneytesting.campaign.infra.jpa;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.chutneytesting.scenario.infra.jpa.Scenario;
import com.chutneytesting.scenario.infra.raw.TagListMapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Version;

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

    @ManyToMany
    @JoinTable(
        name = "CAMPAIGN_SCENARIOS",
        joinColumns = {@JoinColumn(name = "CAMPAIGN_ID", referencedColumnName = "ID")},
        inverseJoinColumns = {@JoinColumn(name = "SCENARIO_ID", referencedColumnName = "ID")}
    )
    @OrderColumn(name = "RANK")
    private List<Scenario> scenarios;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "campaign")
    private Set<CampaignParameter> parameters;

    public Campaign() {
    }

    public Campaign(String title) {
        this(null, title, "", null, false, false, null, null, null, null, null);
    }

    public Campaign(String title, List<Scenario> scenarios) {
        this(null, title, "", null, false, false, null, null, null, scenarios, null);
    }

    public Campaign(Long id, String title, String description, String environment, boolean parallelRun, boolean retryAuto, String datasetId, List<String> tags, Integer version, List<Scenario> scenarios, Set<CampaignParameter> parameters) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.environment = environment;
        this.parallelRun = parallelRun;
        this.retryAuto = retryAuto;
        this.datasetId = datasetId;
        this.tags = TagListMapper.tagsListToString(tags);
        this.version = ofNullable(version).orElse(1);
        this.scenarios = scenarios;
        fromCampaignParameters(parameters);
    }

    public static Campaign fromDomain(com.chutneytesting.server.core.domain.scenario.campaign.Campaign campaign, List<Scenario> scenarios, Integer version) {
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
            new ArrayList<>(scenarios),
            CampaignParameter.fromDomain(campaign)
        );
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
            scenarios.stream().map(Scenario::id).map(String::valueOf).toList(),
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

    public List<Scenario> scenarios() {
        return scenarios;
    }

    public Set<CampaignParameter> parameters() {
        return parameters;
    }

    public Integer version() {
        return version;
    }

    public void removeScenario(Scenario scenario) {
        scenarios.remove(scenario);
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
