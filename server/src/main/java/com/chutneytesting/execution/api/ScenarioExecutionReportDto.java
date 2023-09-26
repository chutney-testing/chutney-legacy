package com.chutneytesting.execution.api;

import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ScenarioExecutionReportDto(long executionId, String scenarioName, String environment, String user,
                                         Map<String, Object> contextVariables, StepExecutionReportCoreDto report) {
}




