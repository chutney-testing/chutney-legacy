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

package com.chutneytesting.execution.api;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
class ScenarioExecutionHistoryController {

    private final ExecutionHistoryRepository executionHistoryRepository;

    ScenarioExecutionHistoryController(ExecutionHistoryRepository executionHistoryRepository) {
        this.executionHistoryRepository = executionHistoryRepository;
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/api/ui/scenario/{scenarioId}/execution/v1", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExecutionSummaryDto> listExecutions(@PathVariable("scenarioId") String scenarioId) {
        return ExecutionSummaryDto.toDto(
            executionHistoryRepository.getExecutions(scenarioId));
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/api/ui/scenario/execution/{executionId}/summary/v1", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExecutionSummaryDto getExecutionSummary(@PathVariable("executionId") Long executionId) {
        return ExecutionSummaryDto.toDto(executionHistoryRepository.getExecutionSummary(executionId));
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/api/ui/scenario/{scenarioId}/execution/{executionId}/v1", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExecutionHistory.Execution getExecutionReport(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        return executionHistoryRepository.getExecution(scenarioId, executionId); // TODO - return ExecutionReportDto
    }
}
