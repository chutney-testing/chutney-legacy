package com.chutneytesting.execution.domain.compiler;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.WebConfiguration;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ComposedTestCaseParametersResolutionPreProcessorTest {

    private final ObjectMapper objectMapper = new WebConfiguration().objectMapper();
    private GlobalvarRepository globalvarRepository;

    @BeforeEach
    public void setUp() {
        globalvarRepository = mock(GlobalvarRepository.class);
        Map<String, String> map = new HashMap<>();
        map.put("key.1", "value1");
        map.put("key.2", "value2");
        when(globalvarRepository.getFlatMap()).thenReturn(map);
    }

    @Test
    public void should_replace_composed_scenario_parameters_with_scoped_execution_parameters_values() {
        // Given
        String actionName = "simple action on target %1$s";
        StepImplementation actionImplementation = new StepImplementation("http-get", "**target**", emptyMap(), emptyMap());
        String stepName = "step with %1$s - %2$s - %3$s";
        String testCaseTitle = "test case testCaseTitle with parameter %1$s";
        String testCaseDescription = "test case description with parameter %1$s - %2$s";
        String environment = "exec env";

        Strategy retryStrategy =
            new Strategy("retry", Maps.of("timeout", "10 s", "delay", "10 s"));

        ExecutableComposedStep action = ExecutableComposedStep.builder()
            .withName(format(actionName, "**target**"))
            .withStrategy(retryStrategy)
            .withParameters(singletonMap("target", "default target"))
            .withImplementation(Optional.of(actionImplementation))
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

        Map<String, String> executionParameters = Maps.of(
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
                    Maps.of(
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
