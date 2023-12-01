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
