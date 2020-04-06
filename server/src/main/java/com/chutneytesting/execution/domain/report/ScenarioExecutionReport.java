package com.chutneytesting.execution.domain.report;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScenarioExecutionReport {
    public final long executionId;
    public final String scenarioName;
    public final String environment;
    public final StepExecutionReportCore report;

    public ScenarioExecutionReport(@JsonProperty("executionId") long executionId,
                                   @JsonProperty("scenarioName") String scenarioName,
                                   @JsonProperty("environment") String environment,
                                   @JsonProperty("report") StepExecutionReportCore report) {
        this.executionId = executionId;
        this.scenarioName = scenarioName;
        this.environment = environment;
        this.report = report;
    }

}
