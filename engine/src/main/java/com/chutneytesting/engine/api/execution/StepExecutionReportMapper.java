package com.chutneytesting.engine.api.execution;

import static java.util.Collections.EMPTY_MAP;

import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import java.util.Map;
import java.util.stream.Collectors;

class StepExecutionReportMapper {

    private StepExecutionReportMapper() {
    }

    static StepExecutionReportDto toDto(StepExecutionReport report) {
        return new StepExecutionReportDto(
            report.name,
            report.environment,
            report.startDate,
            report.duration,
            StatusMapper.toDto(report.status),
            report.information,
            report.errors,
            report.steps.stream().map(StepExecutionReportMapper::toDto).collect(Collectors.toList()),
            StepContextMapper.toDto(report.scenarioContext, report.evaluatedInputs, report.stepResults),
            report.type,
            report.targetName,
            report.targetUrl,
            report.strategy
        );
    }

    static class StepContextMapper {

        @SuppressWarnings("unchecked")
        static StepExecutionReportDto.StepContextDto toDto(Map<String, Object> scenarioContext, Map<String, Object> evaluatedInput, Map<String, Object> stepResults) {
            return new StepExecutionReportDto.StepContextDto(
                scenarioContext != null ? scenarioContext : EMPTY_MAP,
                evaluatedInput != null ? evaluatedInput : EMPTY_MAP,
                stepResults != null ? stepResults : EMPTY_MAP
            );
        }

    }

    static class StatusMapper {
        static StatusDto toDto(Status status) {
            return StatusDto.valueOf(status.name());
        }
    }
}
