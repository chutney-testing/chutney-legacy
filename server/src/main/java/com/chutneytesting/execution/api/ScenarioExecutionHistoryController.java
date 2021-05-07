package com.chutneytesting.execution.api;

import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;

import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ScenarioExecutionHistoryController {

    private final ExecutionHistoryRepository executionHistoryRepository;

    ScenarioExecutionHistoryController(ExecutionHistoryRepository executionHistoryRepository) {
        this.executionHistoryRepository = executionHistoryRepository;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/api/ui/scenario/{scenarioId}/execution/v1", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExecutionSummaryDto> listExecutions(@PathVariable("scenarioId") String scenarioId) {
        return ExecutionSummaryDto.toDto(
            executionHistoryRepository.getExecutions(
                fromFrontId(Optional.of(scenarioId))));
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/api/ui/scenario/{scenarioId}/execution/{executionId}/v1", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExecutionHistory.Execution getExecutionReport(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        return executionHistoryRepository.getExecution(fromFrontId(Optional.of(scenarioId)), executionId);
    }
}
