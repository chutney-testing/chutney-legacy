package com.chutneytesting.execution.domain.compiler;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.chutneytesting.execution.domain.scenario.composed.StepImplementation;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ComposedTestCasePreProcessorTest {

    private final ObjectMapper objectMapper = new WebConfiguration().objectMapper();
    private final String environment = "exec env";
    private final String userId = "exec user";

    private GlobalvarRepository globalvarRepository;
    private DataSetRepository dataSetRepository;

    private ComposedTestCasePreProcessor sut;

    @BeforeEach
    public void setUp() {
        globalvarRepository = mock(GlobalvarRepository.class);
        dataSetRepository = mock(DataSetRepository.class);
    }

    @Test
    public void loop_strategy_should_not_affect_parameters_replacement() {
        // setup
        Map<String, String> map = new HashMap<>();
        map.put("key.1", "value1");
        map.put("key.2", "value2");
        when(globalvarRepository.getFlatMap()).thenReturn(map);

        sut = new ComposedTestCasePreProcessor(objectMapper, globalvarRepository, dataSetRepository);

        // Given
        String actionName = "simple action on target %1$s";
        StepImplementation actionImplementation = new StepImplementation("http-get", "**target**", emptyMap(), emptyMap());
        String stepName = "step with %1$s - %2$s - %3$s";
        String testCaseTitle = "test case testCaseTitle with parameter %1$s";
        String testCaseDescription = "test case description with parameter %1$s";

        Strategy retryStrategy =
            new Strategy("retry", Maps.of("timeout", "10 s", "delay", "10 s"));

        ExecutableComposedStep action = ExecutableComposedStep.builder()
            .withName(format(actionName, "**target**"))
            .withStrategy(retryStrategy)
            .withParameters(singletonMap("target", "default target"))
            .withImplementation(of(actionImplementation))
            .build();

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withName(format(stepName, "**step param**", "**step target**", "**target**"))
            .withParameters(
                Maps.of(
                    "step param", "default step param",
                    "step target", "default step target"
                )
            )
            .withSteps(
                asList(
                    // aliasing child parameter name
                    buildStepFromActionWithDataSet(action, "**step target**"),
                    // make child parameter go up in step parameter
                    buildStepFromActionWithDataSet(action, ""),
                    // use default value of child parameter
                    buildStepFromActionWithDataSet(action, "default target")
                )
            )
            .build();

        Map<String, String> dataSet = Maps.of(
            "testcase title", "A part of testcase title",
            "testcase description", "A part of testcase description",
            "testcase param", "dataset testcase param",
            "testcase target", "default testcase target",
            "step target", "dataset step target",
            "target", "dataset target",
            "step param", "dataset step param"
        );

        ExecutableComposedTestCase ExecutableComposedTestCase = new ExecutableComposedTestCase(
            TestCaseMetadataImpl.builder()
                .withCreationDate(Instant.now())
                .withTitle(format(testCaseTitle, "**testcase title**"))
                .withDescription(format(testCaseDescription, "**testcase description**"))
                .build(),
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    asList(
                        buildStepFromStepWithDataSet(step, "**testcase param**", "", "hard testcase target"),
                        buildStepFromStepWithDataSet(step, "hard testcase param", "**testcase target**", ""),
                        buildStepFromStepWithDataSet(step, "", "hard testcase step target", "**testcase target**")
                    )
                )
                .withParameters(
                    Maps.of(
                        "testcase title", "",
                        "testcase description", "default testcase description",
                        "testcase param", "",
                        "testcase target", "default testcase target"
                    )
                )
                .build(),
            dataSet);

        // When
        final ExecutableComposedTestCase executableComposedTestCaseProcessed = sut.apply(
            new ExecutionRequest(ExecutableComposedTestCase, environment, userId)
        );

        // Then
        assertThat(executableComposedTestCaseProcessed.id()).isEqualTo(ExecutableComposedTestCase.id());
        assertThat(executableComposedTestCaseProcessed.metadata.title()).isEqualTo(format(testCaseTitle, dataSet.get("testcase title")));
        assertThat(executableComposedTestCaseProcessed.metadata.description()).isEqualTo(format(testCaseDescription, dataSet.get("testcase description")));

        ExecutableComposedStep firstStep = executableComposedTestCaseProcessed.composedScenario.composedSteps.get(0);
        assertThat(firstStep.name).isEqualTo(format(stepName, dataSet.get("testcase param"), dataSet.get("step target"), firstStep.executionParameters.get("target")));
        assertThat(firstStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(firstStep.executionParameters).containsOnly(
            entry("step param", dataSet.get("testcase param")),
            entry("step target", dataSet.get("step target")),
            entry("target", ExecutableComposedTestCase.composedScenario.composedSteps.get(0).executionParameters.get("target"))
        );
        assertStepActions(actionName, firstStep, step.steps.get(2).executionParameters.get("target"), retryStrategy);

        ExecutableComposedStep secondStep = executableComposedTestCaseProcessed.composedScenario.composedSteps.get(1);
        assertThat(secondStep.name).isEqualTo(format(stepName, secondStep.executionParameters.get("step param"), dataSet.get("testcase target"), dataSet.get("target")));
        assertThat(secondStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(secondStep.executionParameters).containsOnly(
            entry("step param", ExecutableComposedTestCase.composedScenario.composedSteps.get(1).executionParameters.get("step param")),
            entry("step target", dataSet.get("testcase target")),
            entry("target", dataSet.get("target"))
        );
        assertStepActions(actionName, secondStep, step.steps.get(2).executionParameters.get("target"), retryStrategy);

        ExecutableComposedStep thirdStep = executableComposedTestCaseProcessed.composedScenario.composedSteps.get(2);
        assertThat(thirdStep.name).isEqualTo(format(stepName, dataSet.get("step param"), thirdStep.executionParameters.get("step target"), dataSet.get("testcase target")));
        assertThat(thirdStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(thirdStep.executionParameters).containsOnly(
            entry("step param", dataSet.get("step param")),
            entry("step target", ExecutableComposedTestCase.composedScenario.composedSteps.get(2).executionParameters.get("step target")),
            entry("target", dataSet.get("testcase target"))
        );
        assertStepActions(actionName, thirdStep, step.steps.get(2).executionParameters.get("target"), retryStrategy);
    }

    @Test
    public void should_generate_composed_scenario_steps_with_dataset_values() {
        // Given
        Map<String, String> globalVars = new HashMap<>();
        globalVars.put("global.key", "global var value");
        when(globalvarRepository.getFlatMap()).thenReturn(globalVars);

        String dataSetId = "666";
        DataSet dataSet = DataSet.builder()
            .withId(dataSetId)
            .withConstants(
                Maps.of(
                    "testcase param third", "dataset uv 1"
                )
            )
            .withDatatable(
                asList( // Note that first key has only two distinct values ...
                    Maps.of("testcase param", "dataset mv 11", "testcase param second", "dataset mv 12"),
                    Maps.of("testcase param", "dataset mv 11", "testcase param second", "dataset mv 22"),
                    Maps.of("testcase param", "dataset mv 31", "testcase param second", "dataset mv 32")
                )
            )
            .build();
        when(dataSetRepository.findById(dataSetId)).thenReturn(
            dataSet
        );
        sut = new ComposedTestCasePreProcessor(objectMapper, globalvarRepository, dataSetRepository);

        Map<String, String> executionParameters = Maps.of(
            "testcase title param", "default title value",
            "testcase param", "",
            "testcase param second", "with default value",
            "testcase param third", ""
        );
        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            TestCaseMetadataImpl.builder()
                .withTitle("testcase title for dataset constant ref **testcase title param**")
                .withDescription("testcase description for global var ref **global.key**")
                .withDatasetId(dataSetId)
                .build(),
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    asList(
                        // We want here to have only two iterations
                        ExecutableComposedStep.builder()
                            .withName("step with iteration over one dataset's key **first step param**")
                            .withSteps(
                                singletonList(
                                    ExecutableComposedStep.builder()
                                        .withName("substep name with **testcase param third**")
                                        .withExecutionParameters(
                                            Maps.of("testcase param third", "")
                                        )
                                        .build()
                                )
                            )
                            .withExecutionParameters(
                                Maps.of(
                                    "first step param", "**testcase param**",
                                    "testcase param third", ""
                                )
                            )
                            .build(),
                        // We want here to not iterate at all
                        ExecutableComposedStep.builder()
                            .withName("step do not iterate over me")
                            .withImplementation(of(new StepImplementation("http-get", "**step 2 param** and **testcase param**", emptyMap(), emptyMap())))
                            .withExecutionParameters(
                                Maps.of(
                                    "step 2 param", "hard value 2",
                                    "testcase param", "another hard value 2"
                                )
                            )
                            .build(),
                        // We want here to have three iterations
                        ExecutableComposedStep.builder()
                            .withName("step with iteration over two dataset's keys **testcase param**")
                            .withImplementation(of(new StepImplementation("http-**global.key**", "**testcase param second** and **step 3 param**", emptyMap(), emptyMap())))
                            .withExecutionParameters(
                                Maps.of(
                                    "testcase param", "",
                                    "testcase param second", "",
                                    "step 3 param", "hard value 3"
                                )
                            )
                            .build()
                    )
                )
                .build(),
            executionParameters
        );

        // When
        ExecutableComposedTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", true, userId)
        );

        // Then
        assertThat(processedTestCase.metadata.title()).isEqualTo("testcase title for dataset constant ref default title value");
        assertThat(processedTestCase.metadata.description()).isEqualTo("testcase description for global var ref global var value");

        assertThat(processedTestCase.composedScenario.composedSteps)
            .hasSize(testCase.composedScenario.composedSteps.size());

        ExecutableComposedStep firstScenarioStep = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(firstScenarioStep.name).isEqualTo("step with iteration over one dataset's key **first step param**");
        assertThat(firstScenarioStep.steps).hasSize(2);
        assertThat(firstScenarioStep.steps.get(0).name).isEqualTo("step with iteration over one dataset's key dataset mv 11 - datatable iteration 1");
        assertThat(firstScenarioStep.steps.get(0).steps.get(0).name).isEqualTo("substep name with dataset uv 1");
        assertThat(firstScenarioStep.steps.get(1).name).isEqualTo("step with iteration over one dataset's key dataset mv 31 - datatable iteration 2");
        assertThat(firstScenarioStep.steps.get(1).steps.get(0).name).isEqualTo("substep name with dataset uv 1");

        ExecutableComposedStep secondScenarioStep = processedTestCase.composedScenario.composedSteps.get(1);
        assertThat(secondScenarioStep.name).isEqualTo("step do not iterate over me");
        assertThat(secondScenarioStep.stepImplementation).isEqualTo(of(new StepImplementation("http-get", "hard value 2 and another hard value 2", emptyMap(), emptyMap())));
        assertThat(secondScenarioStep.steps).isEmpty();

        ExecutableComposedStep thirdScenarioStep = processedTestCase.composedScenario.composedSteps.get(2);
        assertThat(thirdScenarioStep.name).isEqualTo("step with iteration over two dataset's keys **testcase param**");
        assertThat(thirdScenarioStep.steps).hasSize(3);
        assertThat(thirdScenarioStep.steps.get(0).name).isEqualTo("step with iteration over two dataset's keys dataset mv 11 - datatable iteration 1");
        assertThat(thirdScenarioStep.steps.get(0).stepImplementation).isEqualTo(of(new StepImplementation("http-global var value", "dataset mv 12 and hard value 3", emptyMap(), emptyMap())));
        assertThat(thirdScenarioStep.steps.get(1).name).isEqualTo("step with iteration over two dataset's keys dataset mv 11 - datatable iteration 2");
        assertThat(thirdScenarioStep.steps.get(1).stepImplementation).isEqualTo(of(new StepImplementation("http-global var value", "dataset mv 22 and hard value 3", emptyMap(), emptyMap())));
        assertThat(thirdScenarioStep.steps.get(2).name).isEqualTo("step with iteration over two dataset's keys dataset mv 31 - datatable iteration 3");
        assertThat(thirdScenarioStep.steps.get(2).stepImplementation).isEqualTo(of(new StepImplementation("http-global var value", "dataset mv 32 and hard value 3", emptyMap(), emptyMap())));
    }

    private ExecutableComposedStep buildStepFromActionWithDataSet(ExecutableComposedStep action, String targetDataSetValue) {
        return ExecutableComposedStep.builder()
            .from(action)
            .withExecutionParameters(
                Maps.of(
                    "target", targetDataSetValue
                )
            )
            .build();
    }

    private ExecutableComposedStep buildStepFromStepWithDataSet(ExecutableComposedStep step, String stepParamDataSetValue, String stepTargetDataSetValue, String targetDataSetValue) {
        return ExecutableComposedStep.builder()
            .from(step)
            .withExecutionParameters(
                Maps.of(
                    "step param", stepParamDataSetValue,
                    "step target", stepTargetDataSetValue,
                    "target", targetDataSetValue
                )
            )
            .build();
    }

    private void assertStepActions(String actionName,
                                   ExecutableComposedStep step,
                                   String thirdActionTargetValue,
                                   Strategy strategy) {

        ExecutableComposedStep thirdStepFirstAction = step.steps.get(0);
        assertThat(thirdStepFirstAction.name).isEqualTo(format(actionName, step.executionParameters.get("step target")));
        assertThat(thirdStepFirstAction.strategy).isEqualTo(strategy);
        assertThat(thirdStepFirstAction.executionParameters).containsOnly(
            entry("target", step.executionParameters.get("step target"))
        );
        ExecutableComposedStep thirdStepSecondAction = step.steps.get(1);
        assertThat(thirdStepSecondAction.name).isEqualTo(format(actionName, thirdStepSecondAction.executionParameters.get("target")));
        assertThat(thirdStepSecondAction.strategy).isEqualTo(strategy);
        assertThat(thirdStepSecondAction.executionParameters).containsOnly(
            entry("target", step.executionParameters.get("target"))
        );
        ExecutableComposedStep thirdStepThirdAction = step.steps.get(2);
        assertThat(thirdStepThirdAction.name).isEqualTo(format(actionName, thirdStepThirdAction.executionParameters.get("target")));
        assertThat(thirdStepThirdAction.strategy).isEqualTo(strategy);
        assertThat(thirdStepThirdAction.executionParameters).containsOnly(
            entry("target", thirdActionTargetValue)
        );
    }

}

