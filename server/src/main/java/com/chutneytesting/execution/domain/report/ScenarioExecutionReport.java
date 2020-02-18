package com.chutneytesting.execution.domain.report;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScenarioExecutionReport {
    public final long executionId;
    public final String scenarioName;
    public final StepExecutionReportCore report;

    public ScenarioExecutionReport(@JsonProperty("executionId") long executionId,
                                   @JsonProperty("scenarioName") String scenarioName,
                                   @JsonProperty("report") StepExecutionReportCore report) {
        this.executionId = executionId;
        this.scenarioName = scenarioName;
        this.report = report;
    }

}
