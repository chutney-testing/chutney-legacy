package com.chutneytesting.component.execution.api;

import com.chutneytesting.component.execution.domain.ExecutableComposedScenario;
import com.chutneytesting.component.execution.domain.ExecutableComposedStep;
import com.chutneytesting.component.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.component.execution.domain.ExecutableStepRepository;
import com.chutneytesting.component.scenario.infra.OrientComposableTestCaseRepository;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.ScenarioExecutionEngine;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotParsableException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.security.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
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
public class ComponentExecutionUiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentExecutionUiController.class);
    private final ScenarioExecutionEngine executionEngine;
    private final ObjectMapper reportObjectMapper;

    private final OrientComposableTestCaseRepository testCaseRepository;

    private final ExecutableStepRepository stepRepository;
    private final UserService userService;

    ComponentExecutionUiController(
        ScenarioExecutionEngine executionEngine,
        @Qualifier("reportObjectMapper") ObjectMapper reportObjectMapper,
        OrientComposableTestCaseRepository testCaseRepository,
        ExecutableStepRepository stepRepository,
        UserService userService
    ) {
        this.executionEngine = executionEngine;
        this.reportObjectMapper = reportObjectMapper;
        this.testCaseRepository = testCaseRepository;
        this.stepRepository = stepRepository;
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('COMPONENT_WRITE')")
    @PostMapping(path = "/api/ui/componentstep/execution/v1/{componentId}/{env}")
    public String executeComponent(@PathVariable("componentId") String componentId, @PathVariable("env") String env) throws IOException {
        LOGGER.debug("executeComponent for componentId={{}] on env [{}]", componentId, env);
        ExecutableComposedStep composedStep = stepRepository.findExecutableById(componentId);
        String userId = userService.currentUserId();
        ScenarioExecutionReport report = execute(composedStep, env, userId);
        return reportObjectMapper.writeValueAsString(report);
    }

    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE')")
    @PostMapping(path = "/api/ui/component/execution/v1/{scenarioId}/{env}")
    public String executeScenario(@PathVariable("scenarioId") String scenarioId, @PathVariable("env") String env) throws IOException {
        LOGGER.debug("executeScenario for scenarioId='{}'", scenarioId);
        Optional<TestCase> executableByTestCase = testCaseRepository.findExecutableById(scenarioId);
        if (executableByTestCase.isPresent()) {
            String userId = userService.currentUserId();
            ScenarioExecutionReport report = executionEngine.simpleSyncExecution(new ExecutionRequest(executableByTestCase.get(), env, userId));
            return reportObjectMapper.writeValueAsString(report);
        } else {
            throw new ScenarioNotFoundException(scenarioId);
        }
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
