package com.chutneytesting.execution.infra.execution;

import static java.util.Collections.EMPTY_MAP;

import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.execution.domain.report.StepExecutionReportCore;
import com.chutneytesting.execution.domain.report.StepExecutionReportCoreBuilder;
import java.util.stream.Collectors;

class StepExecutionReportMapperCore {

    private StepExecutionReportMapperCore() {
    }

    static StepExecutionReportCore fromDto(StepExecutionReportDto reportDto) {
        return new StepExecutionReportCoreBuilder()
            .setName(reportDto.name)
            .setDuration(reportDto.duration)
            .setStartDate(reportDto.startDate)
            .setStatus(ReportStatusMapper.fromDto(reportDto.status))
            .setInformation(reportDto.information)
            .setErrors(reportDto.errors)
            .setSteps(reportDto.steps.stream().map(StepExecutionReportMapperCore::fromDto).collect(Collectors.toList()))
            .setEvaluatedInputs(reportDto.context != null ? reportDto.context.evaluatedInputs : EMPTY_MAP)
            .setType(reportDto.type)
            .setTargetName(reportDto.targetName)
            .setTargetUrl(reportDto.targetUrl)
            .setStrategy(reportDto.strategy)
            .createStepExecutionReport();
    }

    private static class ReportStatusMapper {
        public static ServerReportStatus fromDto(StatusDto status) {
            return ServerReportStatus.valueOf(status.name());
        }
    }
}
