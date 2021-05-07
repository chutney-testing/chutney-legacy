package com.chutneytesting.design.api.campaign.dto;

import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
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
