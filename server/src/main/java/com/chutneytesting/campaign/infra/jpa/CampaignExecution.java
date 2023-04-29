package com.chutneytesting.campaign.infra.jpa;

import static java.util.Collections.emptyList;

import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity(name = "CAMPAIGN_EXECUTIONS")
public class CampaignExecution {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CAMPAIGN_ID")
    private Long campaignId;

    @OneToMany(mappedBy = "campaignExecution")
    private List<ScenarioExecution> scenarioExecutions;

    @Column(name = "PARTIAL")
    private Boolean partial;

    @Column(name = "ENVIRONMENT")
    private String environment;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "DATASET_ID")
    private String datasetId;

    @Column(name = "DATASET_VERSION")
    private Integer datasetVersion;

    @Column(name = "VERSION")
    @Version
    private Integer version;

    public CampaignExecution() {
    }

    public CampaignExecution(Long campaignId) {
        this(null, campaignId, null, null, null, null, null, null, null);
    }

    public CampaignExecution(Long id, Long campaignId, List<ScenarioExecution> scenarioExecutions, Boolean partial, String environment, String userId, String datasetId, Integer datasetVersion, Integer version) {
        this.id = id;
        this.campaignId = campaignId;
        this.scenarioExecutions = scenarioExecutions;
        this.partial = partial;
        this.environment = environment;
        this.userId = userId;
        this.datasetId = datasetId;
        this.datasetVersion = datasetVersion;
        this.version = version;
    }

    public Long id() {
        return id;
    }

    public Long campaignId() {
        return campaignId;
    }

    public void updateFromDomain(CampaignExecutionReport report, Iterable<ScenarioExecution> scenarioExecutions) {
        //id = report.executionId;
        //campaignId = report.campaignId;
        partial = report.partialExecution;
        environment = report.executionEnvironment;
        userId = report.userId;
        datasetId = report.dataSetId.orElse(null);
        datasetVersion = report.dataSetVersion.orElse(null);
        this.scenarioExecutions.clear();
        scenarioExecutions.forEach(se -> {
            se.forCampaignExecution(this);
            this.scenarioExecutions.add(se);
        });
    }

    public CampaignExecutionReport toDomain(Campaign campaign) {
        return toDomain(campaign, true);
    }

    public CampaignExecutionReport toDomain(Campaign campaign, boolean withScenariosExecutions) {
        List<ScenarioExecutionReportCampaign> scenarioExecutionReportCampaigns = emptyList();
        if (withScenariosExecutions) {
            scenarioExecutionReportCampaigns = campaign.scenarios().stream()
                .map(s ->
                    scenarioExecutions.stream()
                        .filter(se -> Objects.equals(se.scenarioId(), s.id()))
                        .findFirst()
                        .map(se -> new ScenarioExecutionReportCampaign(se.scenarioId().toString(), se.scenarioTitle(), se.toDomain()))
                        .orElseGet(() -> new ScenarioExecutionReportCampaign(s.id().toString(), s.title(), blankScenarioExecution(s.title()))))
                .collect(Collectors.toCollection(ArrayList::new));
        }

        return new CampaignExecutionReport(
            id,
            campaignId,
            scenarioExecutionReportCampaigns,
            campaign.title(),
            partial,
            environment,
            datasetId,
            datasetVersion,
            userId);
    }

    private ExecutionHistory.ExecutionSummary blankScenarioExecution(String testCaseTitle) {
        return ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(-1L)
            .testCaseTitle(testCaseTitle)
            .time(LocalDateTime.now())
            .status(ServerReportStatus.NOT_EXECUTED)
            .duration(0)
            .environment("")
            .user("")
            .build();
    }
}
