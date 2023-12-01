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

package com.chutneytesting.campaign.api.dto;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioExecutionReportOutlineDto {
    private String scenarioId;
    private String scenarioName;
    private ExecutionHistory.ExecutionSummary execution;

    public ScenarioExecutionReportOutlineDto(String scenarioId,
                                             String scenarioName,
                                             ExecutionHistory.ExecutionSummary execution) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.execution = execution;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public Long getExecutionId() {
        return execution.executionId();
    }

    public long getDuration() {
        return execution.duration();
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public LocalDateTime getStartDate() {
        return execution.time();
    }

    public ServerReportStatus getStatus() {
        return execution.status();
    }

    public String getInfo() {
        return execution.info().orElse("");
    }

    public String getError() {
        return execution.error().orElse("");
    }

    ExecutionHistory.ExecutionSummary getExecution() {
        return execution;
    }
}
