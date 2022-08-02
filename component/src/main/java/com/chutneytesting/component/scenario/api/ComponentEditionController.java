package com.chutneytesting.component.scenario.api;

import static com.chutneytesting.component.scenario.api.ComposableTestCaseMapper.fromDto;
import static com.chutneytesting.component.scenario.api.ComposableTestCaseMapper.toDto;
import static java.time.Instant.now;

import com.chutneytesting.component.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.component.scenario.api.dto.ComposableTestCaseDto;
import com.chutneytesting.component.scenario.domain.ComposableTestCase;
import com.chutneytesting.server.core.execution.ExecutionRequest;
import com.chutneytesting.server.core.execution.processor.TestCasePreProcessors;
import com.chutneytesting.server.core.scenario.AggregatedRepository;
import com.chutneytesting.server.core.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.scenario.TestCase;
import com.chutneytesting.server.core.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.security.UserService;
import com.chutneytesting.server.core.tools.ui.KeyValue;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(ComponentEditionController.BASE_URL)
public class ComponentEditionController {

    static final String BASE_URL = "/api/scenario/component-edition";

    private final AggregatedRepository<ComposableTestCase> composableTestCaseRepository;
    private final UserService userService;
    private final TestCasePreProcessors testCasePreProcessors;

    public ComponentEditionController(AggregatedRepository<ComposableTestCase> composableTestCaseRepository, UserService userService, TestCasePreProcessors testCasePreProcessors) {
        this.composableTestCaseRepository = composableTestCaseRepository;
        this.userService = userService;
        this.testCasePreProcessors = testCasePreProcessors;
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String saveTestCase(@RequestBody ComposableTestCaseDto composableTestCaseDto) {
        ComposableTestCase composableTestCase = fromDto(composableTestCaseDto);
        composableTestCase = new ComposableTestCase(
            composableTestCase.id,
            TestCaseMetadataImpl.TestCaseMetadataBuilder.from(composableTestCase.metadata)
                .withUpdateDate(now())
                .withAuthor(userService.currentUserId())
                .build(),
            composableTestCase.composableScenario
        );
        return composableTestCaseRepository.save(composableTestCase);
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/{testCaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ComposableTestCaseDto getTestCase(@PathVariable("testCaseId") String testCaseId) {
        ComposableTestCase composableTestCase = composableTestCaseRepository.findById(testCaseId).orElseThrow(() -> new ScenarioNotFoundException(testCaseId));
        return toDto(composableTestCase);
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @GetMapping(path = "/{testCaseId}/executable/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<KeyValue> getTestCaseExecutionParameters(@PathVariable("testCaseId") String testCaseId) {
        return toDto(composableTestCaseRepository.findById(testCaseId).orElseThrow(() -> new ScenarioNotFoundException(testCaseId))).executionParameters();
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/{testCaseId}/executable", produces = MediaType.APPLICATION_JSON_VALUE)
    public ComposableTestCaseDto getExecutableTestCase(@PathVariable("testCaseId") String testCaseId) {
        TestCase testCase = composableTestCaseRepository.findById(testCaseId).orElseThrow(() -> new ScenarioNotFoundException(testCaseId));
        ExecutableComposedTestCase result = testCasePreProcessors.apply(new ExecutionRequest(testCase, "env", "userId"));

        return ExecutableComposableTestCaseMapper.toDto(result);
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @DeleteMapping(path = "/{testCaseId}")
    public void removeScenarioById(@PathVariable("testCaseId") String testCaseId) {
        // TODO dependency to the database to delete execution in this table :
        // CAMPAIGN_EXECUTION_HISTORY
        // CAMPAIGN_SCENARIOS
        composableTestCaseRepository.removeById(testCaseId);
    }
}
