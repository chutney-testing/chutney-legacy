package com.chutneytesting.component.execution.api;

import com.chutneytesting.component.execution.domain.ExecutableComposedScenario;
import com.chutneytesting.component.execution.domain.ExecutableComposedStep;
import com.chutneytesting.component.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.component.execution.domain.ExecutableStepRepository;
import com.chutneytesting.component.scenario.domain.ComposableTestCase;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.ScenarioExecutionEngine;
import com.chutneytesting.execution.domain.ScenarioExecutionReport;
import com.chutneytesting.scenario.domain.AggregatedRepository;
import com.chutneytesting.scenario.domain.ScenarioNotFoundException;
import com.chutneytesting.scenario.domain.ScenarioNotParsableException;
import com.chutneytesting.scenario.domain.TestCase;
import com.chutneytesting.scenario.domain.TestCaseMetadataImpl;
import com.chutneytesting.security.domain.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class ScenarioExecutionUiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioExecutionUiController.class);
    private final ScenarioExecutionEngine executionEngine;
    private final ObjectMapper reportObjectMapper;

    private final AggregatedRepository<ComposableTestCase> testCaseRepository;
    private final ExecutableStepRepository stepRepository;
    private final UserService userService;

    ScenarioExecutionUiController(
        ScenarioExecutionEngine executionEngine, // TODO mettre dans le "server-domain-SPI"
        @Qualifier("reportObjectMapper") ObjectMapper reportObjectMapper,
        AggregatedRepository<ComposableTestCase> testCaseRepository, ExecutableStepRepository stepRepository,
        UserService userService
    ) {
        this.executionEngine = executionEngine;
        this.reportObjectMapper = reportObjectMapper;
        this.testCaseRepository = testCaseRepository;
        this.stepRepository = stepRepository;
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('COMPONENT_WRITE')")
    @PostMapping(path = "/api/ui/component/execution/v1/{componentId}/{env}")
    public String executeComponent(@PathVariable("componentId") String componentId, @PathVariable("env") String env) throws IOException {
        LOGGER.debug("executeComponent for componentId={{}] on env [{}]", componentId, env);
        ExecutableComposedStep composedStep = stepRepository.findExecutableById(componentId);
        String userId = userService.currentUserId();
        ScenarioExecutionReport report = execute(composedStep, env, userId);
        return reportObjectMapper.writeValueAsString(report);
    }

    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE')")
    @PostMapping(path = "/api/ui/scenario/execution/v1/{scenarioId}/{env}")
    public String executeScenario(@PathVariable("scenarioId") String scenarioId, @PathVariable("env") String env) throws IOException {
        LOGGER.debug("executeScenario for scenarioId='{}'", scenarioId);
        TestCase testCase = testCaseRepository.findById(scenarioId).orElseThrow(() -> new ScenarioNotFoundException(scenarioId));
        String userId = userService.currentUserId();
        ScenarioExecutionReport report = executionEngine.simpleSyncExecution(new ExecutionRequest(testCase, env, userId));
        return reportObjectMapper.writeValueAsString(report);
    }

    public ScenarioExecutionReport execute(ExecutableComposedStep composedStep, String environment, String userId) throws ScenarioNotFoundException, ScenarioNotParsableException {
        TestCase testCase = new ExecutableComposedTestCase(
            TestCaseMetadataImpl.builder()
                .withDescription(composedStep.name)
                .withTitle(composedStep.name)
                .build(),
            ExecutableComposedScenario.builder()
                .withComposedSteps(Collections.singletonList(composedStep))
                .build());

        return executionEngine.simpleSyncExecution(
            new ExecutionRequest(testCase, environment, userId)
        );
    }

}
