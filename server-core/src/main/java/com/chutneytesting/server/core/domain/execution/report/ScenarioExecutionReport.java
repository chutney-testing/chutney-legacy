package com.chutneytesting.server.core.domain.execution.report;

public class ScenarioExecutionReport {
    public final long executionId;
    public final String scenarioName;
    public final String environment;
    public final String user;
    public final StepExecutionReportCore report;

    public ScenarioExecutionReport(long executionId,
                                   String scenarioName,
                                   String environment,
                                   String user,
                                   StepExecutionReportCore report) {
        this.executionId = executionId;
        this.scenarioName = scenarioName;
        this.environment = environment;
        this.user = user;
        this.report = report;
    }

}
