package com.chutneytesting.execution.domain.compiler;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.Strategy;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.execution.domain.ExecutionRequest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.Before;
import org.junit.Test;

public class ComposableTestCaseParametersResolutionPreProcessorTest {

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
        String actionImplementation = "{\"identifier\": \"http-get\", \"target\": \"%1$s\", \"inputs\": []}";
        String stepName = "step with %1$s - %2$s - %3$s";
        String testCaseTitle = "test case testCaseTitle with parameter %1$s";
        String testCaseDescription = "test case description with parameter %1$s - %2$s";
        String environment = "exec env";

        Strategy retryStrategy =
            new Strategy("retry", Maps.of("timeout", "10 s", "delay", "10 s"));

        FunctionalStep action = FunctionalStep.builder()
            .withId("1")
            .withName(format(actionName, "**target**"))
            .withStrategy(retryStrategy)
            .withParameters(singletonMap("target", "default target"))
            .withImplementation(ofNullable(format(actionImplementation, "**target**")))
            .build();

        FunctionalStep step = FunctionalStep.builder()
            .withId("2")
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

        ComposableTestCase composableTestCase = new ComposableTestCase(
            "1",
            TestCaseMetadataImpl.builder()
                .withCreationDate(Instant.now())
                .withTitle(format(testCaseTitle, "**testcase title**"))
                .withDescription(format(testCaseDescription, "**testcase description**", "**environment**"))
                .build(),
            ComposableScenario.builder()
                .withFunctionalSteps(
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

        ComposableTestCaseParametersResolutionPreProcessor sut = new ComposableTestCaseParametersResolutionPreProcessor(globalvarRepository);
        // When
        final ComposableTestCase composableTestCaseProcessed = sut.apply(
            new ExecutionRequest(composableTestCase, environment, "user")
        );

        // Then
        assertThat(composableTestCaseProcessed.id()).isEqualTo(composableTestCase.id());
        assertThat(composableTestCaseProcessed.metadata.title()).isEqualTo(format(testCaseTitle, dataSet.get("testcase title")));
        assertThat(composableTestCaseProcessed.metadata.description()).isEqualTo(format(testCaseDescription, dataSet.get("testcase description"), environment));

        FunctionalStep firstStep = composableTestCaseProcessed.composableScenario.functionalSteps.get(0);
        assertThat(firstStep.name).isEqualTo(format(stepName, dataSet.get("testcase param"), dataSet.get("step target"), firstStep.dataSet.get("target")));
        assertThat(firstStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(firstStep.dataSet).containsOnly(
            entry("step param", dataSet.get("testcase param")),
            entry("step target", dataSet.get("step target")),
            entry("target", composableTestCase.composableScenario.functionalSteps.get(0).dataSet.get("target"))
        );
        assertStepActions(actionName, firstStep, step.steps.get(2).dataSet.get("target"), retryStrategy);

        FunctionalStep secondStep = composableTestCaseProcessed.composableScenario.functionalSteps.get(1);
        assertThat(secondStep.name).isEqualTo(format(stepName, secondStep.dataSet.get("step param"), dataSet.get("testcase target"), dataSet.get("target")));
        assertThat(secondStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(secondStep.dataSet).containsOnly(
            entry("step param", composableTestCase.composableScenario.functionalSteps.get(1).dataSet.get("step param")),
            entry("step target", dataSet.get("testcase target")),
            entry("target", dataSet.get("target"))
        );
        assertStepActions(actionName, secondStep, step.steps.get(2).dataSet.get("target"), retryStrategy);

        FunctionalStep thirdStep = composableTestCaseProcessed.composableScenario.functionalSteps.get(2);
        assertThat(thirdStep.name).isEqualTo(format(stepName, dataSet.get("step param"), thirdStep.dataSet.get("step target"), dataSet.get("testcase target")));
        assertThat(thirdStep.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(thirdStep.dataSet).containsOnly(
            entry("step param", dataSet.get("step param")),
            entry("step target", composableTestCase.composableScenario.functionalSteps.get(2).dataSet.get("step target")),
            entry("target", dataSet.get("testcase target"))
        );
        assertStepActions(actionName, thirdStep, step.steps.get(2).dataSet.get("target"), retryStrategy);
    }


    private FunctionalStep buildStepFromActionWithDataSet(FunctionalStep action, String targetDataSetValue) {
        return FunctionalStep.builder()
            .from(action)
            .overrideDataSetWith(
                Maps.of(
                    "target", targetDataSetValue
                )
            )
            .build();
    }

    private FunctionalStep buildStepFromStepWithDataSet(FunctionalStep step, String stepParamDataSetValue, String stepTargetDataSetValue, String targetDataSetValue) {
        return FunctionalStep.builder()
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
                                   FunctionalStep step,
                                   String thirdActionTargetValue,
                                   Strategy strategy) {

        FunctionalStep thirdStepFirstAction = step.steps.get(0);
        assertThat(thirdStepFirstAction.name).isEqualTo(format(actionName, step.dataSet.get("step target")));
        assertThat(thirdStepFirstAction.strategy).isEqualTo(strategy);
        assertThat(thirdStepFirstAction.dataSet).containsOnly(
            entry("target", step.dataSet.get("step target"))
        );
        FunctionalStep thirdStepSecondAction = step.steps.get(1);
        assertThat(thirdStepSecondAction.name).isEqualTo(format(actionName, thirdStepSecondAction.dataSet.get("target")));
        assertThat(thirdStepSecondAction.strategy).isEqualTo(strategy);
        assertThat(thirdStepSecondAction.dataSet).containsOnly(
            entry("target", step.dataSet.get("target"))
        );
        FunctionalStep thirdStepThirdAction = step.steps.get(2);
        assertThat(thirdStepThirdAction.name).isEqualTo(format(actionName, thirdStepThirdAction.dataSet.get("target")));
        assertThat(thirdStepThirdAction.strategy).isEqualTo(strategy);
        assertThat(thirdStepThirdAction.dataSet).containsOnly(
            entry("target", thirdActionTargetValue)
        );
    }

}
