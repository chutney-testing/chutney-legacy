package com.chutneytesting.execution.api;

import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record StepExecutionReportCoreDto(String name, Long duration, Instant startDate, ServerReportStatus status,
                                  List<String> information, List<String> errors, List<StepExecutionReportCoreDto> steps,
                                  String type, String targetName, String targetUrl, String strategy,
                                  Map<String, Object> evaluatedInputs, Map<String, Object> stepOutputs) {
}
