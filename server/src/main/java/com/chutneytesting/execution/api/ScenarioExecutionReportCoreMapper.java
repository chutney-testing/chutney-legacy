package com.chutneytesting.execution.api;

import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScenarioExecutionReportCoreMapper {
    ScenarioExecutionReportDto toDto(ScenarioExecutionReport source);
    ScenarioExecutionReport toDomain(ScenarioExecutionReportDto destination);
}
