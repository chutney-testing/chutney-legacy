package com.chutneytesting.execution.api;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.dataset.domain.DataSetRepository;
import com.chutneytesting.execution.domain.GwtScenarioMarshaller;
import com.chutneytesting.scenario.api.raw.mapper.GwtScenarioMapper;
import com.chutneytesting.scenario.domain.gwt.GwtScenario;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.security.infra.SpringUserService;
import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.ScenarioExecutionEngine;
import com.chutneytesting.server.core.domain.execution.ScenarioExecutionEngineAsync;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
import com.chutneytesting.tools.ui.MyMixInForIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Observable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

    private final ScenarioExecutionEngine executionEngine;
    private final ScenarioExecutionEngineAsync executionEngineAsync;
    private final TestCaseRepository testCaseRepository;
    private final ObjectMapper objectMapper;
    private final ObjectMapper reportObjectMapper;
    private final SpringUserService userService;
    private final DataSetRepository datasetRepository;
    private final ScenarioExecutionReportMapper scenarioExecutionReportMapper;

    ScenarioExecutionUiController(
        ScenarioExecutionEngine executionEngine,
        ScenarioExecutionEngineAsync executionEngineAsync,
        TestCaseRepository testCaseRepository,
        ObjectMapper objectMapper,
        SpringUserService userService,
        DataSetRepository datasetRepository,
        ScenarioExecutionReportMapper scenarioExecutionReportMapper) {
        this.executionEngine = executionEngine;
        this.executionEngineAsync = executionEngineAsync;
        this.testCaseRepository = testCaseRepository;
        this.objectMapper = objectMapper;
        this.reportObjectMapper = dtoReportObjectMapper();
        this.userService = userService;
        this.datasetRepository = datasetRepository;
        this.scenarioExecutionReportMapper = scenarioExecutionReportMapper;
    }

    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE')")
    @PostMapping(path = "/api/idea/scenario/execution/{env}")
    public String executeScenarioWitRawContent(@RequestBody IdeaRequest ideaRequest, @PathVariable("env") String env) throws IOException {
        LOGGER.debug("execute Scenario v2 for content='{}' with parameters '{}'", ideaRequest.getContent(), ideaRequest.getParams());
        String userId = userService.currentUser().getId();
        GwtScenario gwtScenario = marshaller.deserialize("test title for idea", "test description for idea", ideaRequest.getContent());

        TestCase testCase = GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withDescription("test description for idea")
                .withTitle("test title for idea")
                .build())
            .withScenario(gwtScenario)
            .withExecutionParameters(ideaRequest.getParams())
            .build();

        ScenarioExecutionReport report = executionEngine.simpleSyncExecution(
            new ExecutionRequest(testCase, env, userId)
        );

        return objectMapper.writeValueAsString(report);
    }

    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE')")
    @PostMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/{env}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String executeScenarioAsyncWithExecutionParameters(@PathVariable("scenarioId") String scenarioId, @PathVariable("env") String env, @RequestBody List<KeyValue> executionParametersKV) {
        LOGGER.debug("execute async scenario '{}' using parameters '{}'", scenarioId, executionParametersKV);
        TestCase testCase = testCaseRepository.findExecutableById(scenarioId).orElseThrow(() -> new ScenarioNotFoundException(scenarioId));
        Map<String, String> executionParameters = KeyValue.toMap(executionParametersKV);
        String userId = userService.currentUser().getId();
        DataSet dataset = getDataSet(testCase);
        return executionEngineAsync.execute(new ExecutionRequest(requireNonNull(testCase.usingExecutionParameters(requireNonNull(executionParameters))), env, userId, dataset)).toString();
    }

    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE')")
    @PostMapping(path = "/api/ui/scenario/execution/v1/{scenarioId}/{env}")
    public String executeScenario(@PathVariable("scenarioId") String scenarioId, @PathVariable("env") String env) throws IOException {
        LOGGER.debug("executeScenario for scenarioId='{}'", scenarioId);
        TestCase testCase = testCaseRepository.findExecutableById(scenarioId).orElseThrow(() -> new ScenarioNotFoundException(scenarioId));
        String userId = userService.currentUserId();
        DataSet dataset = getDataSet(testCase);
        ScenarioExecutionReport report = executionEngine.simpleSyncExecution(new ExecutionRequest(testCase, env, userId, dataset));

        return reportObjectMapper.writeValueAsString(this.scenarioExecutionReportMapper.toDto(report));
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/execution/{executionId}")
    public Flux<ServerSentEvent<String>> followScenarioExecution(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        LOGGER.debug("followScenarioExecution for scenarioId='{}' and executionID='{}'", scenarioId, executionId);
        return createScenarioExecutionSSEFlux(
            executionEngineAsync.followExecution(scenarioId, executionId)
        );
    }

    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE')")
    @PostMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/execution/{executionId}/stop")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void stopExecution(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        LOGGER.debug("Stop for scenarioId='{}' and executionID='{}'", scenarioId, executionId);
        executionEngineAsync.stop(scenarioId, executionId);
    }

    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE')")
    @PostMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/execution/{executionId}/pause")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void pauseExecution(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        LOGGER.debug("Pause for scenarioId='{}' and executionID='{}'", scenarioId, executionId);
        executionEngineAsync.pause(scenarioId, executionId);
    }

    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE')")
    @PostMapping(path = "/api/ui/scenario/executionasync/v1/{scenarioId}/execution/{executionId}/resume")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void resumeExecution(@PathVariable("scenarioId") String scenarioId, @PathVariable("executionId") Long executionId) {
        LOGGER.debug("Resume for scenarioId='{}' and executionID='{}'", scenarioId, executionId);
        executionEngineAsync.resume(scenarioId, executionId);
    }

    private Flux<ServerSentEvent<String>> createScenarioExecutionSSEFlux(Observable<ScenarioExecutionReport> scenarioExecutionReports) {
        return Flux.from(scenarioExecutionReports.map(
            reportEvent -> ServerSentEvent.<String>builder()
                .id(String.valueOf(reportEvent.executionId))
                .event(reportEvent.report.isTerminated() ? "last" : "partial")
                .data(reportObjectMapper.writeValueAsString(reportEvent))
                .build()
        ).toFlowable(BackpressureStrategy.BUFFER));
    }

    private DataSet getDataSet(TestCase testCase) {
        String defaultDatasetId = testCase.metadata().defaultDataset();
        if (!defaultDatasetId.isEmpty()) {
            return datasetRepository.findById(defaultDatasetId);
        } else {
            return DataSet.NO_DATASET;
        }
    }

    // TODO - Use Spring serialization
    public ObjectMapper dtoReportObjectMapper() {
        SimpleModule jdomElementModule = new SimpleModule();
        jdomElementModule.addSerializer(Element.class, new JDomElementSerializer());

        return new ObjectMapper()
            .addMixIn(Resource.class, MyMixInForIgnoreType.class)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature())
            .registerModule(jdomElementModule)
            .findAndRegisterModules();
    }

    // TODO - To remove when Reporter will serialize itself
    static class JDomElementSerializer extends StdSerializer<Element> {

        private static final long serialVersionUID = 1L;

        protected JDomElementSerializer() {
            this(null);
        }

        protected JDomElementSerializer(Class<Element> t) {
            super(t);
        }

        @Override
        public void serialize(Element element, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            String xmlString = new XMLOutputter(Format.getCompactFormat()).outputString(element);
            jsonGenerator.writeObject(xmlString);
        }
    }
}
