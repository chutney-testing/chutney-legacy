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
        return executionHistoryRepository.getExecution(scenarioId, executionId);
    }
}
