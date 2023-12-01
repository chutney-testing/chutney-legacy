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

package com.chutneytesting.execution.domain.campaign;

import com.chutneytesting.jira.api.ReportForJira;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraReportMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraReportMapper.class);

    public static ReportForJira from(String stringReport, ObjectMapper objectMapper) {
        try {
            ScenarioExecutionReport scenarioReport = objectMapper.readValue(stringReport, ScenarioExecutionReport.class);

            return new ReportForJira(
                scenarioReport.report.startDate,
                scenarioReport.report.duration,
                scenarioReport.report.status.name(),
                createStep(scenarioReport.report),
                scenarioReport.environment);

        } catch (IOException e) {
            LOGGER.error("Cannot deserialize scenarioReport", e);
            return null;
        }
    }

    private static ReportForJira.Step createStep(StepExecutionReportCore coreStep) {
        return new ReportForJira.Step(coreStep.name, coreStep.errors, coreStep.steps.stream().map(JiraReportMapper::createStep).collect(Collectors.toList()));
    }

}
