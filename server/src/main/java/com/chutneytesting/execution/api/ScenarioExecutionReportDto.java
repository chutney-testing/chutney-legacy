package com.chutneytesting.execution.api;

import java.util.Map;

public record ScenarioExecutionReportDto(long executionId, String scenarioName, String environment, String user,
                                         Map<String, Object> contextVariables, StepExecutionReportCoreDto report) {
}
