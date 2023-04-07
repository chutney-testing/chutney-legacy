package com.chutneytesting.campaign.infra.jpa;

import static java.util.stream.Collectors.toMap;

import com.chutneytesting.scenario.infra.jpa.Scenario;
import com.chutneytesting.scenario.infra.raw.TagListMapper;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
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
        this(null, title, "", null, false, false, null, null, null, null);
    }

    public Campaign(String title, List<Scenario> scenarios) {
        this(null, title, "", null, false, false, null, null, scenarios, null);
    }

    public Campaign(Long id, String title, String description, String environment, boolean parallelRun, boolean retryAuto, String datasetId, List<String> tags, List<Scenario> scenarios, Set<CampaignParameter> parameters) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.environment = environment;
        this.parallelRun = parallelRun;
        this.retryAuto = retryAuto;
        this.datasetId = datasetId;
        this.tags = TagListMapper.tagsListToString(tags);
        this.scenarios = scenarios;
        Optional.ofNullable(parameters).ifPresent(params -> {
            params.forEach(param -> param.forCampaign(this));
        });
        this.parameters = parameters;
    }

    public static Campaign fromDomain(com.chutneytesting.server.core.domain.scenario.campaign.Campaign campaign, List<Scenario> scenarios) {
        return new Campaign(
            campaign.id,
            campaign.title,
            campaign.description,
            campaign.executionEnvironment(),
            campaign.parallelRun,
            campaign.retryAuto,
            campaign.externalDatasetId,
            campaign.tags,
            scenarios,
            CampaignParameter.fromDomain(campaign)
        );
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

    public List<Scenario> scenarios() {
        return scenarios;
    }

    public Set<CampaignParameter> parameters() {
        return parameters;
    }
}
