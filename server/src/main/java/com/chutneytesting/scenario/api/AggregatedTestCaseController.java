package com.chutneytesting.scenario.api;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.chutneytesting.execution.api.ExecutionSummaryDto;
import com.chutneytesting.scenario.api.raw.dto.TestCaseIndexDto;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/scenario/v2")
public class AggregatedTestCaseController {

    private final TestCaseRepository testCaseRepository;
    private final ExecutionHistoryRepository executionHistoryRepository;

    public AggregatedTestCaseController(TestCaseRepository testCaseRepository, ExecutionHistoryRepository executionHistoryRepository) {
        this.testCaseRepository = testCaseRepository;
        this.executionHistoryRepository = executionHistoryRepository;
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/{testCaseId}/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    public TestCaseIndexDto testCaseMetaData(@PathVariable("testCaseId") String testCaseId) {
        TestCase testCase = testCaseRepository.findById(testCaseId).orElseThrow(() -> new ScenarioNotFoundException(testCaseId));
        return TestCaseIndexDto.from(testCase.metadata());
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ') or hasAuthority('CAMPAIGN_WRITE')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TestCaseIndexDto> getTestCases(@RequestParam( name = "textFilter", required = false) String textFilter) {
        List<TestCaseMetadata> testCases = isNullOrEmpty(textFilter) ? testCaseRepository.findAll() : testCaseRepository.search(textFilter);
        Map<String, ExecutionSummary> lastExecutions = executionHistoryRepository.getLastExecutions(testCases.stream().map(TestCaseMetadata::id).collect(Collectors.toList()));

        return testCases.stream()
            .map((tc) -> {
                if(lastExecutions.get(tc.id()) != null) {
                    ExecutionSummaryDto execution = ExecutionSummaryDto.toDto(lastExecutions.get(tc.id()));
                    return TestCaseIndexDto.from(tc, execution);
                } else {
                    return TestCaseIndexDto.from(tc);
                }
            })
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @DeleteMapping(path = "/{testCaseId}")
    public void removeScenarioById(@PathVariable("testCaseId") String testCaseId) {
        testCaseRepository.removeById(testCaseId);
    }
}
