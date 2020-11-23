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
import org.junit.Before;
import org.junit.Test;

public class ComposedTestCaseParametersResolutionPreProcessorTest {

    private final ObjectMapper objectMapper = new WebConfiguration().objectMapper();
    private GlobalvarRepository globalvarRepository;

    @Before
    public void setUp() {
        globalvarRepository = mock(GlobalvarRepository.class);
        Map<String, String> map = new HashMap<>();
        map.put("key.1", "value1");
        map.put("key.2", "value2");
        when(globalvarRepository.getFlatMap()).thenReturn(map);
    }

    @Test
    public void should_replace_composableTestCase_scenario_parameters_with_scoped_data_set_values() {
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

        Map<String, String> dataSet = Maps.of(
            "testcase title", "A part of testcase title",
            "testcase description", "A part of testcase description",
            "testcase param", "dataset testcase param",
            "testcase target", "default testcase target",
            "step target", "dataset step target",
            "target", "dataset target",
            "step param", "dataset step param"
        );

        ExecutableComposedTestCase composableTestCase = new ExecutableComposedTestCase(
            "1",
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
            dataSet);

        ComposedTestCaseParametersResolutionPreProcessor sut = new ComposedTestCaseParametersResolutionPreProcessor(globalvarRepository, objectMapper);
        // When
        final ExecutableComposedTestCase composableTestCaseProcessed = sut.apply(
            new ExecutionRequest(composableTestCase, environment, "user")
        );

        // Then
        assertThat(composableTestCaseProcessed.id()).isEqualTo(composableTestCase.id());
        assertThat(composableTestCaseProcessed.metadata.title()).isEqualTo(format(testCaseTitle, dataSet.get("testcase title")));
        assertThat(composableTestCaseProcessed.metadata.description()).isEqualTo(format(testCaseDescription, dataSet.get("testcase description"), environment));

        ExecutableComposedStep firstStep = composableTestCaseProcessed.composedScenario.composedSteps.get(0);
        assertThat(firstStep.name).isEqualTo(format(stepName, dataSet.get("testcase param"), dataSet.get("step target"), firstStep.dataset.get("target")));
        assertThat(firstStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(firstStep.dataset).containsOnly(
            entry("step param", dataSet.get("testcase param")),
            entry("step target", dataSet.get("step target")),
            entry("target", composableTestCase.composedScenario.composedSteps.get(0).dataset.get("target"))
        );
        assertStepActions(actionName, firstStep, step.steps.get(2).dataset.get("target"), retryStrategy);

        ExecutableComposedStep secondStep = composableTestCaseProcessed.composedScenario.composedSteps.get(1);
        assertThat(secondStep.name).isEqualTo(format(stepName, secondStep.dataset.get("step param"), dataSet.get("testcase target"), dataSet.get("target")));
        assertThat(secondStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(secondStep.dataset).containsOnly(
            entry("step param", composableTestCase.composedScenario.composedSteps.get(1).dataset.get("step param")),
            entry("step target", dataSet.get("testcase target")),
            entry("target", dataSet.get("target"))
        );
        assertStepActions(actionName, secondStep, step.steps.get(2).dataset.get("target"), retryStrategy);

        ExecutableComposedStep thirdStep = composableTestCaseProcessed.composedScenario.composedSteps.get(2);
        assertThat(thirdStep.name).isEqualTo(format(stepName, dataSet.get("step param"), thirdStep.dataset.get("step target"), dataSet.get("testcase target")));
        assertThat(thirdStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(thirdStep.dataset).containsOnly(
            entry("step param", dataSet.get("step param")),
            entry("step target", composableTestCase.composedScenario.composedSteps.get(2).dataset.get("step target")),
            entry("target", dataSet.get("testcase target"))
        );
        assertStepActions(actionName, thirdStep, step.steps.get(2).dataset.get("target"), retryStrategy);
    }


    private ExecutableComposedStep buildStepFromActionWithDataSet(ExecutableComposedStep action, String targetDataSetValue) {
        return ExecutableComposedStep.builder()
            .from(action)
            .overrideDataSetWith(
                Maps.of(
                    "target", targetDataSetValue
                )
            )
            .build();
    }

    private ExecutableComposedStep buildStepFromStepWithDataSet(ExecutableComposedStep step, String stepParamDataSetValue, String stepTargetDataSetValue, String targetDataSetValue) {
        return ExecutableComposedStep.builder()
            .from(step)
            .overrideDataSetWith(
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
        assertThat(thirdStepFirstAction.name).isEqualTo(format(actionName, step.dataset.get("step target")));
        assertThat(thirdStepFirstAction.strategy).isEqualTo(strategy);
        assertThat(thirdStepFirstAction.dataset).containsOnly(
            entry("target", step.dataset.get("step target"))
        );
        ExecutableComposedStep thirdStepSecondAction = step.steps.get(1);
        assertThat(thirdStepSecondAction.name).isEqualTo(format(actionName, thirdStepSecondAction.dataset.get("target")));
        assertThat(thirdStepSecondAction.strategy).isEqualTo(strategy);
        assertThat(thirdStepSecondAction.dataset).containsOnly(
            entry("target", step.dataset.get("target"))
        );
        ExecutableComposedStep thirdStepThirdAction = step.steps.get(2);
        assertThat(thirdStepThirdAction.name).isEqualTo(format(actionName, thirdStepThirdAction.dataset.get("target")));
        assertThat(thirdStepThirdAction.strategy).isEqualTo(strategy);
        assertThat(thirdStepThirdAction.dataset).containsOnly(
            entry("target", thirdActionTargetValue)
        );
    }

}
