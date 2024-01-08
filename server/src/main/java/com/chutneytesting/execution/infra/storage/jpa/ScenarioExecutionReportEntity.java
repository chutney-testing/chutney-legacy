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

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import jakarta.persistence.Basic;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.ZoneId;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity(name = "SCENARIO_EXECUTIONS_REPORTS")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ScenarioExecutionReportEntity {

    @Id
    @Column(name = "SCENARIO_EXECUTION_ID")
    private Long scenarioExecutionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "SCENARIO_EXECUTION_ID")
    private ScenarioExecutionEntity scenarioExecution;

    @Column(name = "REPORT")
    @Basic(fetch = FetchType.LAZY)
    private String report;

    @Column(name = "VERSION")
    @Version
    private Integer version;

    public ScenarioExecutionReportEntity() {
    }

    public ScenarioExecutionReportEntity(ScenarioExecutionEntity scenarioExecution, String report) {
        this.scenarioExecutionId = scenarioExecution.id();
        this.scenarioExecution = scenarioExecution;
        this.report = report;
    }

    public void updateReport(ExecutionHistory.Execution execution) {
        report = execution.report();
    }
    public String getReport() {
        return report;
    }
    public ExecutionHistory.Execution toDomain() {
        return ImmutableExecutionHistory.Execution.builder()
            .executionId(scenarioExecutionId)
            .time(Instant.ofEpochMilli(scenarioExecution.executionTime()).atZone(ZoneId.systemDefault()).toLocalDateTime())
            .duration(scenarioExecution.duration())
            .status(scenarioExecution.status())
            .info(ofNullable(scenarioExecution.information()))
            .error(ofNullable(scenarioExecution.error()))
            .report(report)
            .testCaseTitle(scenarioExecution.scenarioTitle())
            .environment(scenarioExecution.environment())
            .user(scenarioExecution.userId())
            .datasetId(ofNullable(scenarioExecution.datasetId()))
            .datasetVersion(ofNullable(scenarioExecution.datasetVersion()))
            .build();
    }
}
