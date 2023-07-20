package com.chutneytesting.execution.infra.storage.jpa;

import static java.util.Optional.ofNullable;

import com.chutneytesting.campaign.infra.jpa.CampaignExecution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.time.Instant;
import java.time.ZoneId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import org.apache.commons.lang3.StringUtils;

@Entity(name = "SCENARIO_EXECUTIONS")
public class ScenarioExecution {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SCENARIO_ID")
    private String scenarioId;

    @ManyToOne
    @JoinColumn(name = "CAMPAIGN_EXECUTION_ID")
    private CampaignExecution campaignExecution;

    @Column(name = "EXECUTION_TIME")
    private Long executionTime;

    @Column(name = "DURATION")
    private Long duration;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private ServerReportStatus status;

    @Column(name = "INFORMATION")
    private String information;

    @Column(name = "ERROR")
    private String error;

    @Column(name = "SCENARIO_TITLE")
    private String scenarioTitle;

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

    public ScenarioExecution() {
    }

    public ScenarioExecution(Long id, String scenarioId, CampaignExecution campaignExecution, Long executionTime, Long duration, ServerReportStatus status, String information, String error, String scenarioTitle, String environment, String userId, String datasetId, Integer datasetVersion, Integer version) {
        this.id = id;
        this.scenarioId = scenarioId;
        this.campaignExecution = campaignExecution;
        this.executionTime = executionTime;
        this.duration = duration;
        this.status = status;
        this.information = information;
        this.error = error;
        this.scenarioTitle = scenarioTitle;
        this.environment = environment;
        this.userId = userId;
        this.datasetId = datasetId;
        this.datasetVersion = datasetVersion;
        this.version = version;
    }

    public Long id() {
        return id;
    }

    public String scenarioId() {
        return scenarioId;
    }

    public CampaignExecution campaignExecution() {
        return campaignExecution;
    }

    public void forCampaignExecution(CampaignExecution campaignExecution) {
        this.campaignExecution = campaignExecution;
    }

    public void clearCampaignExecution() {
        this.campaignExecution = null;
    }

    public Integer version() {
        return version;
    }

    public Long executionTime() {
        return executionTime;
    }

    public Long duration() {
        return duration;
    }

    public ServerReportStatus status() {
        return status;
    }

    public String information() {
        return information;
    }

    public String error() {
        return error;
    }

    public String scenarioTitle() {
        return scenarioTitle;
    }

    public String environment() {
        return environment;
    }

    public String userId() {
        return userId;
    }

    public String datasetId() {
        return datasetId;
    }

    public Integer datasetVersion() {
        return datasetVersion;
    }

    public static ScenarioExecution fromDomain(String scenarioId, ExecutionHistory.ExecutionProperties execution) {
        return fromDomain(scenarioId, null, null, execution);
    }

    public static ScenarioExecution fromDomain(String scenarioId, Long id, Integer version, ExecutionHistory.ExecutionProperties execution) {
        return new ScenarioExecution(
            id,
            scenarioId,
            null,
            execution.time().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            execution.duration(),
            execution.status(),
            execution.info().map(info -> StringUtils.substring(info, 0, 512)).orElse(null),
            execution.error().map(error -> StringUtils.substring(error, 0, 512)).orElse(null),
            execution.testCaseTitle(),
            execution.environment(),
            execution.user(),
            execution.datasetId().orElse(null),
            execution.datasetVersion().orElse(null),
            version
        );
    }

    public ExecutionHistory.ExecutionSummary toDomain() {
        return toDomain(null);
    }

    public ExecutionHistory.ExecutionSummary toDomain(CampaignExecutionReport campaignReport) {
        return ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(id)
            .time(Instant.ofEpochMilli(executionTime).atZone(ZoneId.systemDefault()).toLocalDateTime())
            .duration(duration)
            .status(status)
            .info(ofNullable(information))
            .error(ofNullable(error))
            .testCaseTitle(scenarioTitle)
            .environment(environment)
            .datasetId(ofNullable(datasetId))
            .datasetVersion(ofNullable(datasetVersion))
            .user(userId)
            .campaignReport(ofNullable(campaignReport))
            .build();
    }

    public void updateFromExecution(ExecutionHistory.Execution execution) {
        duration = execution.duration();
        status = execution.status();
        information = execution.info().orElse(null);
        error = execution.error().orElse(null);
    }
}
