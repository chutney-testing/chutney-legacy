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

package com.chutneytesting.execution.infra.storage.jpa;

import static java.util.Optional.ofNullable;

import com.chutneytesting.campaign.infra.jpa.CampaignExecutionEntity;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import jakarta.persistence.Cacheable;
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
import java.time.Instant;
import java.time.ZoneId;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity(name = "SCENARIO_EXECUTIONS")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ScenarioExecutionEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SCENARIO_ID")
    private String scenarioId;

    @ManyToOne
    @JoinColumn(name = "CAMPAIGN_EXECUTION_ID")
    private CampaignExecutionEntity campaignExecution;

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

    public ScenarioExecutionEntity() {
    }

    public ScenarioExecutionEntity(Long id, String scenarioId, CampaignExecutionEntity campaignExecution, Long executionTime, Long duration, ServerReportStatus status, String information, String error, String scenarioTitle, String environment, String userId, String datasetId, Integer datasetVersion, Integer version) {
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

    public CampaignExecutionEntity campaignExecution() {
        return campaignExecution;
    }

    public void forCampaignExecution(CampaignExecutionEntity campaignExecutionEntity) {
        this.campaignExecution = campaignExecutionEntity;
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

    public static ScenarioExecutionEntity fromDomain(String scenarioId, ExecutionHistory.ExecutionProperties execution) {
        return fromDomain(scenarioId, null, null, execution);
    }

    public static ScenarioExecutionEntity fromDomain(String scenarioId, Long id, Integer version, ExecutionHistory.ExecutionProperties execution) {
        return new ScenarioExecutionEntity(
            id,
            scenarioId,
            null,
            execution.time().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            execution.duration(),
            execution.status(),
            execution.info().map(ScenarioExecutionEntity::truncateExecutionTrace).orElse(null),
            execution.error().map(ScenarioExecutionEntity::truncateExecutionTrace).orElse(null),
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

    public ExecutionHistory.ExecutionSummary toDomain(CampaignExecution campaignReport) {
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
        information = execution.info().map(ScenarioExecutionEntity::truncateExecutionTrace).orElse(null);
        error = execution.error().map(ScenarioExecutionEntity::truncateExecutionTrace).orElse(null);
    }

    private static String truncateExecutionTrace(String trace) {
        return StringUtils.substring(trace, 0, 512);
    }
}
