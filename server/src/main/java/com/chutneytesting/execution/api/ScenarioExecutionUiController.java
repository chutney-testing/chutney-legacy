package com.chutneytesting.execution.api;

import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.isComposableFrontId;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.api.compose.dto.KeyValue;
import com.chutneytesting.design.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.StepRepository;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.report.ScenarioExecutionReport;
import com.chutneytesting.execution.domain.scenario.ScenarioExecutionEngine;
import com.chutneytesting.execution.domain.scenario.ScenarioExecutionEngineAsync;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@CrossOrigin(origins = "*")
@RestController
public class ScenarioExecutionUiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioExecutionUiController.class);
    private final ScenarioExecutionEngine executionEngine;
    private final ScenarioExecutionEngineAsync executionEngineAsync;
    private final TestCaseRepository testCaseRepository;
    private final ComposableTestCaseRepository composableTestCaseRepository;
    private final ObjectMapper objectMapper;
    private final StepRepository stepRepository;

    ScenarioExecutionUiController(ScenarioExecutionEngine executionEngine, ScenarioExecutionEngineAsync executionEngineAsync, TestCaseRepository testCaseRepository, ComposableTestCaseRepository composableTestCaseRepository, ObjectMapper objectMapper, StepRepository stepRepository) {
        this.executionEngine = executionEngine;
        this.executionEngineAsync = executionEngineAsync;
        this.testCaseRepository = testCaseRepository;
        this.composableTestCaseRepository = composableTestCaseRepository;
        this.objectMapper = objectMapper;
        this.stepRepository = stepRepository;
    }

    @PostMapping(path = "/api/ui/scenario/execution/v1/{scenarioId}/{env}")
    public String executeScenario(@PathVariable("scenarioId") String scenarioId, @PathVariable("env") String env) throws IOException {
        LOGGER.debug("executeScenario for scenarioId='{}'", scenarioId);
        TestCase testCase = testCaseRepository.findById(scenarioId);
        ScenarioExecutionReport report = executionEngine.execute(testCase, env);
        return objectMapper.writeValueAsString(report);
    }

    @PostMapping(path = "/api/ui/component/execution/v1/{componentId}/{env}")
    public String executeComponent(@PathVariable("componentId") String componentId, @PathVariable("env") String env) throws IOException {
        LOGGER.debug("executeComponent for componentId={{}] on env [{}]", componentId, env);
        FunctionalStep functionalStep = stepRepository.findById(fromFrontId(Optional.of(componentId)));
        ScenarioExecutionReport report = executionEngine.execute(functionalStep, env);
        return objectMapper.writeValueAsString(report);
    }

    @PostMapping(path = "/api/idea/scenario/execution/{env}")
    public String executeScenarioWitRawContent(@RequestBody IdeaRequest ideaRequest, @PathVariable("env") String env) throws IOException {
        LOGGER.debug("execute Scenario v2 for content='{}' with parameters '{}'", ideaRequest.getContent(), ideaRequest.getParams());
        ScenarioExecutionReport report = executionEngine.execute(ideaRequest.getContent(), ideaRequest.getParams(), env);
        return objectMapper.writeValueAsString(report);
    }

    @PostMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/{env}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String executeScenarioAsyncWithDataSet(@PathVariable("scenarioId") String scenarioId, @PathVariable("env") String env, @RequestBody List<KeyValue> dataSet) {
        LOGGER.debug("executeScenarioAsync for scenarioId='{}' with dataset '{}'", scenarioId, dataSet);
        TestCase testCase;
        if (isComposableFrontId(scenarioId)) {
            testCase = composableTestCaseRepository.findById(fromFrontId(Optional.of(scenarioId)));
        } else {
            testCase = testCaseRepository.findById(scenarioId);
        }
        Map<String, String> inlineDataSet = ofNullable(dataSet)
            .map(KeyValue::toMap)
            .orElseGet(HashMap::new);
        return executionEngineAsync.execute(new ExecutionRequest(testCase, env, inlineDataSet)).toString();
    }

    @GetMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/execution/{executionId}")
    public Flux<ServerSentEvent<ScenarioExecutionReport>> followScenarioExecution(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        LOGGER.debug("followScenarioExecution for scenarioId='{}' and executionID='{}'", scenarioId, executionId);
        return createScenarioExecutionSSEFlux(
            executionEngineAsync.followExecution(scenarioId, executionId)
        );
    }

    @PostMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/execution/{executionId}/stop")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void stopExecution(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        LOGGER.debug("Stop for scenarioId='{}' and executionID='{}'", scenarioId, executionId);
        executionEngineAsync.stop(scenarioId, executionId);
    }

    @PostMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/execution/{executionId}/pause")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void pauseExecution(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        LOGGER.debug("Pause for scenarioId='{}' and executionID='{}'", scenarioId, executionId);
        executionEngineAsync.pause(scenarioId, executionId);
    }

    @PostMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/execution/{executionId}/resume")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void resumeExecution(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        LOGGER.debug("Resume for scenarioId='{}' and executionID='{}'", scenarioId, executionId);
        executionEngineAsync.resume(scenarioId, executionId);
    }

    private Flux<ServerSentEvent<ScenarioExecutionReport>> createScenarioExecutionSSEFlux(Observable<ScenarioExecutionReport> scenarioExecutionReports) {
        return Flux.from(scenarioExecutionReports.map(
            reportEvent -> ServerSentEvent.<ScenarioExecutionReport>builder()
                .id(String.valueOf(reportEvent.executionId))
                .event(reportEvent.report.isTerminated() ? "last" : "partial")
                .data(reportEvent)
                .build()
        ).toFlowable(BackpressureStrategy.BUFFER));
    }
}
