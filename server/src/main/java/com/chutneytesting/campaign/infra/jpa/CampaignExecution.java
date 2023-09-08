package com.chutneytesting.campaign.infra.jpa;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionEntity;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity(name = "CAMPAIGN_EXECUTIONS")
public class CampaignExecution {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CAMPAIGN_ID")
    private Long campaignId;

    @OneToMany(mappedBy = "campaignExecution")
    private List<ScenarioExecutionEntity> scenarioExecutions;

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

    public CampaignExecution(Long id, Long campaignId, List<ScenarioExecutionEntity> scenarioExecutions, Boolean partial, String environment, String userId, String datasetId, Integer datasetVersion, Integer version) {
        this.id = id;
        this.campaignId = campaignId;
        this.scenarioExecutions = scenarioExecutions;
        this.partial = ofNullable(partial).orElse(false);
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

    public List<ScenarioExecutionEntity> scenarioExecutions() {
        return scenarioExecutions;
    }

    public void updateFromDomain(CampaignExecutionReport report, Iterable<ScenarioExecutionEntity> scenarioExecutions) {
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

    public CampaignExecutionReport toDomain(CampaignEntity campaign, boolean isRunning, Function<String, String> titleSupplier) {
        return toDomain(campaign, true, isRunning, titleSupplier);
    }

    public CampaignExecutionReport toDomain(CampaignEntity campaign, boolean withScenariosExecutions, boolean isRunning, Function<String, String> scenarioTitleSupplier) {
        List<ScenarioExecutionReportCampaign> scenarioExecutionReportCampaigns = emptyList();
        if (withScenariosExecutions) {
            if (isRunning) {
                scenarioExecutionReportCampaigns = campaign.campaignScenarios().stream()
                    .map(cs ->
                        scenarioExecutions.stream()
                            .filter(se -> se.scenarioId().equals(cs.scenarioId()))
                            .findFirst()
                            .map(se -> new ScenarioExecutionReportCampaign(se.scenarioId(), se.scenarioTitle(), se.toDomain()))
                            .orElseGet(() -> blankScenarioExecutionReport(cs, scenarioTitleSupplier)))
                    .collect(Collectors.toCollection(ArrayList::new));
            } else {
                scenarioExecutionReportCampaigns = scenarioExecutions.stream()
                    .map(se -> new ScenarioExecutionReportCampaign(se.scenarioId(), se.scenarioTitle(), se.toDomain()))
                    .collect(Collectors.toCollection(ArrayList::new));
            }
        }

        return new CampaignExecutionReport(
            id,
            campaignId,
            scenarioExecutionReportCampaigns,
            campaign.title(),
            ofNullable(partial).orElse(false),
            environment,
            datasetId,
            datasetVersion,
            userId);
    }

    private ScenarioExecutionReportCampaign blankScenarioExecutionReport(CampaignScenario campaignScenario, Function<String, String> titleSupplier) {
        String scenarioTitle = titleSupplier.apply(campaignScenario.scenarioId());
        return new ScenarioExecutionReportCampaign(campaignScenario.scenarioId(), scenarioTitle, blankScenarioExecution(scenarioTitle));
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
