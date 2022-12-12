package com.chutneytesting.engine.infrastructure.delegation;

import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReportBuilder;
import java.util.stream.Collectors;

class StepExecutionReportMapper {

    private StepExecutionReportMapper() {
    }

    static StepExecutionReport fromDto(StepExecutionReportDto reportDto) {
        return new StepExecutionReportBuilder().setName(reportDto.name)
            .setDuration(reportDto.duration)
            .setStartDate(reportDto.startDate)
            .setStatus(StatusMapper.fromDto(reportDto.status))
            .setInformation(reportDto.information)
            .setErrors(reportDto.errors)
            .setSteps(reportDto.steps.stream().map(StepExecutionReportMapper::fromDto).collect(Collectors.toList()))
            .setEvaluatedInputs(reportDto.context.evaluatedInputs)
            .setInputs(reportDto.context.inputs)
            .setScenarioContext(reportDto.context.scenarioContext)
            .setStepResults(reportDto.context.stepResults)
            .setType(reportDto.type)
            .setTargetName(reportDto.targetName)
            .setTargetUrl(reportDto.targetUrl)
            .setStrategy(reportDto.strategy)
            .createStepExecutionReport();
    }

    private static class StatusMapper {
        static Status fromDto(StatusDto status) {
            return Status.valueOf(status.name());
        }
    }
}
