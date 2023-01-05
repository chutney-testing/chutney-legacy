package com.chutneytesting.server.core.domain.execution.report;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScenarioExecutionReport {
    public final long executionId;
    public final String scenarioName;
    public final String environment;
    public final String user;
    public final Map<String, Object> contextVariables;
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
        this.contextVariables = searchContextVariables(report);
        this.report = report;
    }

    private Map<String, Object> searchContextVariables(StepExecutionReportCore report) {
        Map<String, Object> contextVariables = new HashMap<>();
        report.steps.forEach(step -> {
            if (step.steps.isEmpty()) {
                contextVariables.putAll(step.stepOutputs);
            } else {
                contextVariables.putAll(searchContextVariables(step));
            }
        });
        return Collections.unmodifiableMap(contextVariables);
    }
}
