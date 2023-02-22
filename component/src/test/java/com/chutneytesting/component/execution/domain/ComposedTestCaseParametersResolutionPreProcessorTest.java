package com.chutneytesting.component.execution.domain;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.nCopies;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.scenario.domain.gwt.Strategy;
import com.chutneytesting.server.core.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.tests.OrientDatabaseHelperTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

public class ComposedTestCaseParametersResolutionPreProcessorTest {

    private final ObjectMapper objectMapper = OrientDatabaseHelperTest.objectMapper();
    private GlobalvarRepository globalvarRepository;

    @Test
    public void should_replace_composed_scenario_parameters_with_scoped_execution_parameters_values() {
        // Given
        globalvarRepository = mock(GlobalvarRepository.class);
        when(globalvarRepository.getFlatMap()).thenReturn(new HashMap<>());

        String actionName = "simple action on target %1$s";
        StepImplementation actionImplementation = new StepImplementation("http-get", "**target**", emptyMap(), emptyMap(), emptyMap());
        String stepName = "step with %1$s - %2$s - %3$s";
        String testCaseTitle = "test case testCaseTitle with parameter %1$s";
        String testCaseDescription = "test case description with parameter %1$s - %2$s";
        String environment = "exec env";

        Strategy retryStrategy =
            new Strategy("retry", Map.of("timeout", "10 s", "delay", "10 s"));

        ExecutableComposedStep action = ExecutableComposedStep.builder()
            .withName(format(actionName, "**target**"))
            .withStrategy(retryStrategy)
            .withParameters(singletonMap("target", "default target"))
            .withImplementation(Optional.of(actionImplementation))
            .build();

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withName(format(stepName, "**step param**", "**step target**", "**target**"))
            .withParameters(
                Map.of(
                    "step param", "default step param",
                    "step target", "default step target"
                )
            )
            .withSteps(
                Arrays.asList(
                    // aliasing child parameter name
                    buildStepFromActionWithDataSet(action, "**step target**"),
                    // make child parameter go up in step parameter
                    buildStepFromActionWithDataSet(action, ""),
                    // use default value of child parameter
                    buildStepFromActionWithDataSet(action, "default target")
                )
            )
            .build();

        Map<String, String> executionParameters = Map.of(
            "testcase title", "A part of testcase title",
            "testcase description", "A part of testcase description",
            "testcase param", "dataset testcase param",
            "testcase target", "default testcase target",
            "step target", "dataset step target",
            "target", "dataset target",
            "step param", "dataset step param"
        );

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(
            TestCaseMetadataImpl.builder()
                .withCreationDate(Instant.now())
                .withTitle(format(testCaseTitle, "**testcase title**"))
                .withDescription(format(testCaseDescription, "**testcase description**", "**environment**"))
                .build(),
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    Arrays.asList(
                        buildStepFromStepWithDataSet(step, "**testcase param**", "", "hard testcase target"),
                        buildStepFromStepWithDataSet(step, "hard testcase param", "**testcase target**", ""),
                        buildStepFromStepWithDataSet(step, "", "hard testcase step target", "**testcase target**")
                    )
                )
                .withParameters(
                    Map.of(
                        "testcase title", "",
                        "testcase description", "default testcase description",
                        "testcase param", "",
                        "testcase target", "default testcase target"
                    )
                )
                .build(),
            executionParameters);

        ComposedTestCaseParametersResolutionPreProcessor sut = new ComposedTestCaseParametersResolutionPreProcessor(globalvarRepository, objectMapper);
        // When
        final ExecutableComposedTestCase composedTestCaseProcessed = sut.apply(
            new ExecutionRequest(composedTestCase, environment, "user")
        );

        // Then
        assertThat(composedTestCaseProcessed.id()).isEqualTo(composedTestCase.id());
        assertThat(composedTestCaseProcessed.metadata.title()).isEqualTo(format(testCaseTitle, executionParameters.get("testcase title")));
        assertThat(composedTestCaseProcessed.metadata.description()).isEqualTo(format(testCaseDescription, executionParameters.get("testcase description"), environment));

        ExecutableComposedStep firstStep = composedTestCaseProcessed.composedScenario.composedSteps.get(0);
        assertThat(firstStep.name).isEqualTo(format(stepName, executionParameters.get("testcase param"), executionParameters.get("step target"), firstStep.executionParameters.get("target")));
        assertThat(firstStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(firstStep.executionParameters).containsOnly(
            entry("step param", executionParameters.get("testcase param")),
            entry("step target", executionParameters.get("step target")),
            entry("target", composedTestCase.composedScenario.composedSteps.get(0).executionParameters.get("target"))
        );
        assertStepActions(actionName, firstStep, step.steps.get(2).executionParameters.get("target"), retryStrategy);

        ExecutableComposedStep secondStep = composedTestCaseProcessed.composedScenario.composedSteps.get(1);
        assertThat(secondStep.name).isEqualTo(format(stepName, secondStep.executionParameters.get("step param"), executionParameters.get("testcase target"), executionParameters.get("target")));
        assertThat(secondStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(secondStep.executionParameters).containsOnly(
            entry("step param", composedTestCase.composedScenario.composedSteps.get(1).executionParameters.get("step param")),
            entry("step target", executionParameters.get("testcase target")),
            entry("target", executionParameters.get("target"))
        );
        assertStepActions(actionName, secondStep, step.steps.get(2).executionParameters.get("target"), retryStrategy);

        ExecutableComposedStep thirdStep = composedTestCaseProcessed.composedScenario.composedSteps.get(2);
        assertThat(thirdStep.name).isEqualTo(format(stepName, executionParameters.get("step param"), thirdStep.executionParameters.get("step target"), executionParameters.get("testcase target")));
        assertThat(thirdStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(thirdStep.executionParameters).containsOnly(
            entry("step param", executionParameters.get("step param")),
            entry("step target", composedTestCase.composedScenario.composedSteps.get(2).executionParameters.get("step target")),
            entry("target", executionParameters.get("testcase target"))
        );
        assertStepActions(actionName, thirdStep, step.steps.get(2).executionParameters.get("target"), retryStrategy);
    }

    private ExecutableComposedStep buildStepFromActionWithDataSet(ExecutableComposedStep action, String targetDataSetValue) {
        return ExecutableComposedStep.builder()
            .from(action)
            .withExecutionParameters(
                Map.of(
                    "target", targetDataSetValue
                )
            )
            .build();
    }

    private ExecutableComposedStep buildStepFromStepWithDataSet(ExecutableComposedStep step, String stepParamDataSetValue, String stepTargetDataSetValue, String targetDataSetValue) {
        return ExecutableComposedStep.builder()
            .from(step)
            .withExecutionParameters(
                Map.of(
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

    @Test
    public void should_preprocess_global_variables_referencing_other_global_variables_for_replacement() {
        // Given
        globalvarRepository = mock(GlobalvarRepository.class);
        Map<String, String> map = new HashMap<>();
        map.put("id", "ABCDEF");
        map.put("id-name", "ROBERT");
        map.put("id-lastname", "DUPONT");
        map.put("id-mail", "robert.dupont@france.fr");

        map.put("DIRECT_REF", "**id-mail** **id** **id-lastname** **id-name**");
        map.put("DIRECT_REF_REF", "**DIRECT_REF**");
        map.put("DIRECT_REF_REF_REF", "**DIRECT_REF_REF**");

        map.put("PARENT.DIRECT_REF", "**id-mail** **id** **id-lastname** **id-name**");

        map.put("PARENT.id", map.get("id"));
        map.put("PARENT.id-name", map.get("id-name"));
        map.put("PARENT.id-lastname", map.get("id-lastname"));
        map.put("PARENT.id-mail", map.get("id-mail"));
        map.put("PARENT.DIRECT_PARENT_REF", "**PARENT.id-mail** **PARENT.id** **PARENT.id-lastname** **PARENT.id-name**");

        when(globalvarRepository.getFlatMap()).thenReturn(map);

        String expectedInputsValue = map.get("id-mail") + " " + map.get("id") + " " + map.get("id-lastname") + " " + map.get("id-name");

        StepImplementation actionImplementation = new StepImplementation(
            "context-put",
            null,
            Map.of(
                "direct", "**id-mail** **id** **id-lastname** **id-name**",
                "directRef", "**DIRECT_REF**",
                "directRefRef", "**DIRECT_REF_REF**",
                "directRefRefRef", "**DIRECT_REF_REF_REF**",
                "parentDirectRef", "**PARENT.DIRECT_REF**",
                "parentDirectParentRef", "**PARENT.DIRECT_PARENT_REF**"
            ),
            emptyMap(),
            emptyMap()
        );

        ExecutableComposedStep action = ExecutableComposedStep.builder()
            .withName("name")
            .withImplementation(of(actionImplementation))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(
            TestCaseMetadataImpl.builder()
                .withCreationDate(Instant.now())
                .withTitle("title")
                .build(),
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        action
                    )
                )
                .build(),
            emptyMap());

        ComposedTestCaseParametersResolutionPreProcessor sut = new ComposedTestCaseParametersResolutionPreProcessor(globalvarRepository, objectMapper);
        // When
        final ExecutableComposedTestCase composedTestCaseProcessed = sut.apply(
            new ExecutionRequest(composedTestCase, "ENV", "user")
        );

        // Then
        Map<String, Object> contextPutStepInputs = composedTestCaseProcessed.composedScenario.composedSteps.get(0).stepImplementation.get().inputs;
        assertThat(contextPutStepInputs).contains(
            entry("direct", expectedInputsValue),
            entry("directRef", expectedInputsValue),
            entry("directRefRef", expectedInputsValue),
            entry("directRefRefRef", expectedInputsValue),
            entry("parentDirectRef", expectedInputsValue),
            entry("parentDirectParentRef", expectedInputsValue)
        );
    }

    @Test
    void should_apply_preprocess_in_a_reasonable_timeframe_when_there_are_lots_of_globalvars() {
        // Given
        Arbitrary<String> keys = Arbitraries.strings().ascii().ofLength(15);
        Arbitrary<String> values = Arbitraries.strings().ascii().ofLength(500);
        Map<String, String> globalVars = Arbitraries.maps(keys, values).ofSize(1000).sample();
        globalvarRepository = mock(GlobalvarRepository.class);
        when(globalvarRepository.getFlatMap()).thenReturn(globalVars);

        Map<String, String> parameters = Map.of("param1", "val1", "param2", "val2", "param3", "val3");
        Strategy strategyWithParameters = new Strategy("strategy", Map.of("param1", "val1", "param2", "val2"));
        StepImplementation simpleImplementation = new StepImplementation("type", "target", emptyMap(), emptyMap(), emptyMap());
        ExecutableComposedStep actionStep = ExecutableComposedStep.builder()
            .withName("step action")
            .withStrategy(strategyWithParameters)
            .withParameters(parameters)
            .withImplementation(of(simpleImplementation))
            .build();
        ExecutableComposedStep stepWithActions = ExecutableComposedStep.builder().from(actionStep)
            .withName("step with actions")
            .withSteps(nCopies(3, actionStep))
            .build();
        ExecutableComposedStep stepWithSteps = ExecutableComposedStep.builder().from(actionStep)
            .withName("step with higher steps")
            .withSteps(nCopies(3, stepWithActions))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(
            TestCaseMetadataImpl.builder().withCreationDate(Instant.now()).withTitle("title").withDescription("description").build(),
            ExecutableComposedScenario.builder().withComposedSteps(nCopies(20, stepWithSteps)).withParameters(parameters).build(),
            emptyMap());

        ComposedTestCaseParametersResolutionPreProcessor sut = new ComposedTestCaseParametersResolutionPreProcessor(globalvarRepository, objectMapper);
        // When / Then
        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(
            () -> sut.apply(
                new ExecutionRequest(composedTestCase, "exec env", "user")
            ));
    }
}
