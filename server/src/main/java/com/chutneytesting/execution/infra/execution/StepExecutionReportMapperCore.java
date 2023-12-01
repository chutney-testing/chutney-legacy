/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.execution.infra.execution;

import static java.util.Collections.EMPTY_MAP;

import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCoreBuilder;
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
            .setStepOutputs(reportDto.context != null ? reportDto.context.stepResults : EMPTY_MAP)
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
