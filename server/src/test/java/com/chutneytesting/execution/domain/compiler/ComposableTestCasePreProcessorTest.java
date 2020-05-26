package com.chutneytesting.execution.domain.compiler;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.Strategy;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ComposableTestCasePreProcessorTest {

    private GlobalvarRepository globalvarRepository;
    private ObjectMapper objectMapper = new WebConfiguration().objectMapper();
    private String environment = "exec env";

    private ComposableTestCasePreProcessor sut;

    @BeforeEach
    public void setUp() {
        globalvarRepository = Mockito.mock(GlobalvarRepository.class);
    }

    @Test
    public void should_replace_parameters_and_apply_strategy_when_strategy_input_is_a_parameter_coming_from_global_vars() {
        // setup
        final String VALUE = "value";
        final String PARAM_NAME = "param_name";
        final String GLOBAL_PARAMETER = "global.parameter";

        final String FIRST_ITERATION_VALUE = "iteration_1 ";
        final String SECOND_ITERATION_VALUE = "iteration_2";

        when(globalvarRepository.getFlatMap())
            .thenReturn(new HashMap<>(Maps.of(GLOBAL_PARAMETER, "[{\"" + VALUE + "\":\"" + FIRST_ITERATION_VALUE + "\"}, {\"value\":\"" + SECOND_ITERATION_VALUE + "\"}]")));

        sut = new ComposableTestCasePreProcessor(objectMapper, globalvarRepository);

        // Given
        Map<String, String> childParameters = singletonMap(VALUE,  /* empty: will be provided by Loop strategy */  "");
        FunctionalStep childStepWithParameters = FunctionalStep.builder()
            .withParameters(childParameters)
            .overrideDataSetWith(singletonMap(VALUE,  /* empty: will be provided by Loop strategy */  "**" + VALUE + "**"))
            .build();

        Strategy strategy = new Strategy("Loop", singletonMap("data", "**" + PARAM_NAME + "**"));
        FunctionalStep parentStep = FunctionalStep.builder()
            .withName("Iteration : **" + VALUE + "**")
            .withSteps(singletonList(childStepWithParameters))
            .withStrategy(strategy)
            .withParameters(singletonMap(PARAM_NAME, ""))
            .overrideDataSetWith(singletonMap(PARAM_NAME, /* provided by global variable repository */ "**" + GLOBAL_PARAMETER + "**"))
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(parentStep))
            .build();

        ComposableTestCase composableTestase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(composableTestase, environment);

        // Then
        assertThat(actual.composableScenario.functionalSteps.size()).isEqualTo(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.size()).isEqualTo(2);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).name).isEqualTo("Iteration : " + FIRST_ITERATION_VALUE + " - iteration 1");
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(1).name).isEqualTo("Iteration : " + SECOND_ITERATION_VALUE + " - iteration 2");
    }

    @Test
    public void should_replace_parameters_and_apply_strategy_when_strategy_input_is_a_parameter_provided_by_test_case() {
        // setup
        final String VALUE = "value";
        final String PARAM_NAME = "param_name";
        final String GLOBAL_PARAMETER = "global.parameter";

        final String FIRST_ITERATION_VALUE = "iteration_1 ";
        final String SECOND_ITERATION_VALUE = "iteration_2";

        when(globalvarRepository.getFlatMap())
            .thenReturn(new HashMap<>(Maps.of(GLOBAL_PARAMETER, "[{\"" + VALUE + "\":\"" + FIRST_ITERATION_VALUE + "\"}, {\"value\":\"" + SECOND_ITERATION_VALUE + "\"}]")));

        sut = new ComposableTestCasePreProcessor(objectMapper, globalvarRepository);

        // Given
        Map<String, String> childParameters = singletonMap(VALUE,  /* empty: will be provided by Loop strategy */  "");
        FunctionalStep childStepWithParameters = FunctionalStep.builder()
            .withParameters(childParameters)
            .overrideDataSetWith(singletonMap(VALUE,  /* empty: will be provided by Loop strategy */  "**" + VALUE + "**"))
            .build();

        Strategy strategy = new Strategy("Loop", singletonMap("data", "**" + PARAM_NAME + "**"));
        FunctionalStep parentStep = FunctionalStep.builder()
            .withName("Iteration : **" + VALUE + "**")
            .withSteps(singletonList(childStepWithParameters))
            .withStrategy(strategy)
            .withParameters(singletonMap(PARAM_NAME, ""))
            .overrideDataSetWith(singletonMap(PARAM_NAME, ""))
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(parentStep))
            .build();

        Map<String, String> dataset = singletonMap(PARAM_NAME, "**" + GLOBAL_PARAMETER + "**");
        ComposableTestCase composableTestase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario, dataset);

        // When
        ComposableTestCase actual = sut.apply(composableTestase, environment);

        // Then
        assertThat(actual.composableScenario.functionalSteps.size()).isEqualTo(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.size()).isEqualTo(2);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).name).isEqualTo("Iteration : " + FIRST_ITERATION_VALUE + " - iteration 1");
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(1).name).isEqualTo("Iteration : " + SECOND_ITERATION_VALUE + " - iteration 2");
    }

    @Test
    public void loop_strategy_should_not_affect_parameters_replacement() {
        // setup
        Map<String, String> map = new HashMap<>();
        map.put("key.1", "value1");
        map.put("key.2", "value2");
        when(globalvarRepository.getFlatMap()).thenReturn(map);

        sut = new ComposableTestCasePreProcessor(objectMapper, globalvarRepository);

        // Given
        String actionName = "simple action on target %1$s";
        String actionImplementation = "{\"identifier\": \"http-get\", \"target\": \"%1$s\", \"inputs\": []}";
        String stepName = "step with %1$s - %2$s - %3$s";
        String testCaseTitle = "test case testCaseTitle with parameter %1$s";
        String testCaseDescription = "test case description with parameter %1$s";

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
                .withDescription(format(testCaseDescription, "**testcase description**"))
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

        // When
        final ComposableTestCase composableTestCaseProcessed = sut.apply(composableTestCase, environment);

        // Then
        assertThat(composableTestCaseProcessed.id()).isEqualTo(composableTestCase.id());
        assertThat(composableTestCaseProcessed.metadata.title()).isEqualTo(format(testCaseTitle, dataSet.get("testcase title")));
        assertThat(composableTestCaseProcessed.metadata.description()).isEqualTo(format(testCaseDescription, dataSet.get("testcase description")));

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

    @Test
    public void should_generate_composableTestCase_scenario_steps_with_loop_values() {
        // setup
        Map<String, String> map = new HashMap<>();
        map.put("key.1", "value1");
        map.put("key.2", "value2");
        when(globalvarRepository.getFlatMap()).thenReturn(map);

        sut = new ComposableTestCasePreProcessor(objectMapper, globalvarRepository);

        // Given
        String actionName = "simple action on target %1$s";

        String actionImplementation =
            "{" +
                "\"identifier\": \"http-get\"," +
                "\"target\": \"**target**\", " +
                "\"inputs\": [" +
                "{\"name\":\"target\",\"value\":\"**target**\"" + "}," +
                "{\"name\":\"action param\",\"value\":\"hard action value\"}" +
                "]" +
                "}";

        String stepName = "step name";
        String testCaseTitle = "test case testCaseTitle with parameter %1$s";
        String testCaseDescription = "test case description";

        String loopData =
            "[" +
                "{\"target\" : \"target_it1\"}," +
                "{\"target\" : \"target_it2\", \"action param\" : \"action param value it2\"}" +
                "]";

        Map<String, String> actionParameters = Maps.of(
            "target", "default target"
        );

        FunctionalStep action = FunctionalStep.builder()
            .withId("1")
            .withName(format(actionName, "**target**"))
            .withParameters(actionParameters)
            .withImplementation(of(actionImplementation))
            .build();

        Map<String, String> stepParameters = Maps.of(
            "step param", "default step param",
            "step target", "default step target"
        );

        FunctionalStep step = FunctionalStep.builder()
            .withId("2")
            .withName(stepName)
            .withParameters(stepParameters)
            .withSteps(singletonList(buildStepFromActionWithDataSet(action, "**target**")))
            .withStrategy(new Strategy("Loop", Collections.singletonMap("data", loopData)))
            .build();

        Map<String, String> dataSet = Maps.of(
            "testcase title", "A part of testcase title",
            "testcase description", "A part of testcase description",
            "testcase param", "dataset testcase param",
            "testcase target", "default testcase target",
            "step target", "dataset step target",
            "step param", "dataset step param"
        );

        ComposableTestCase composableTestCase = new ComposableTestCase(
            "1",
            TestCaseMetadataImpl.builder()
                .withCreationDate(Instant.now())
                .withTitle(format(testCaseTitle, "**testcase title**"))
                .withDescription(testCaseDescription)
                .build(),
            ComposableScenario.builder()
                .withFunctionalSteps(
                    Arrays.asList(
                        buildStepFromStepWithDataSet(step, "**testcase param**", ""),
                        buildStepFromStepWithDataSet(step, "", "hard testcase step target")
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

        final ComposableTestCase composableTestCaseProcessed = sut.apply(composableTestCase, environment);

        // Then
        assertThat(composableTestCaseProcessed.id()).isEqualTo(composableTestCase.id());
        assertThat(composableTestCaseProcessed.metadata.title()).isEqualTo(format(testCaseTitle, dataSet.get("testcase title")));
        assertThat(composableTestCaseProcessed.metadata.description()).isEqualTo(testCaseDescription);

        /* Step 1 */
        FunctionalStep step_1 = composableTestCaseProcessed.composableScenario.functionalSteps.get(0);
        assertThat(step_1.steps.size()).isEqualTo(2);
        assertThat(step_1.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_1.dataSet.size()).isEqualTo(3);
        assertThat(step_1.name).isEqualTo(stepName);

        /* Step 1.1 */
        FunctionalStep step_1_1 = step_1.steps.get(0);
        assertThat(step_1_1.name).isEqualTo(stepName.concat(" - iteration 1"));
        assertThat(step_1_1.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_1_1.steps.size()).isEqualTo(1);
        assertThat(step_1_1.steps.get(0).implementation.get()).contains("\"target\": \"target_it1\",");
        assertThat(step_1_1.steps.get(0).implementation.get()).contains("inputs\": [{\"name\":\"target\",\"value\":\"target_it1\"},{\"name\":\"action param\",\"value\":\"hard action value\"}]");
        assertThat(step_1_1.steps.get(0).dataSet).containsOnly(entry("target", "target_it1"));

        /* Step 1.2 */
        FunctionalStep step_1_2 = step_1.steps.get(1);
        assertThat(step_1_2.name).isEqualTo(stepName.concat(" - iteration 2"));
        assertThat(step_1_2.steps.size()).isEqualTo(1);
        assertThat(step_1_2.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_1_2.steps.get(0).implementation.get()).contains("\"target\": \"target_it2\",");
        assertThat(step_1_2.steps.get(0).implementation.get()).contains("inputs\": [{\"name\":\"target\",\"value\":\"target_it2\"},{\"name\":\"action param\",\"value\":\"hard action value\"}]");
        assertThat(step_1_2.steps.get(0).strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_1_2.steps.get(0).dataSet).containsOnly(entry("target", "target_it2"));

        /* Step 2 */
        FunctionalStep step_2 = composableTestCaseProcessed.composableScenario.functionalSteps.get(1);
        assertThat(step_2.steps.size()).isEqualTo(2);
        assertThat(step_2.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_2.dataSet.size()).isEqualTo(2);
        assertThat(step_2.name).isEqualTo(stepName);

        FunctionalStep step_2_1 = step_1.steps.get(0);
        assertThat(step_2_1.name).isEqualTo(stepName.concat(" - iteration 1"));
        assertThat(step_2_1.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_2_1.steps.size()).isEqualTo(1);
        assertThat(step_2_1.steps.get(0).implementation.get()).contains("target_it1");
        assertThat(step_2_1.steps.get(0).implementation.get()).contains("inputs\": [{\"name\":\"target\",\"value\":\"target_it1\"},{\"name\":\"action param\",\"value\":\"hard action value\"}]");
        assertThat(step_2_1.steps.get(0).strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_2_1.steps.get(0).dataSet).containsOnly(
            entry("target", "target_it1")
        );

        FunctionalStep step_2_2 = step_1.steps.get(1);
        assertThat(step_2_2.name).isEqualTo(stepName.concat(" - iteration 2"));
        assertThat(step_2_2.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_2_2.steps.size()).isEqualTo(1);
        assertThat(step_2_2.steps.get(0).implementation.get()).contains("target_it2");
        assertThat(step_2_2.steps.get(0).implementation.get()).contains("inputs\": [{\"name\":\"target\",\"value\":\"target_it2\"},{\"name\":\"action param\",\"value\":\"hard action value\"}]");
        assertThat(step_2_2.steps.get(0).strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_2_2.steps.get(0).dataSet).containsOnly(
            entry("target", "target_it2")
        );
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

    private FunctionalStep buildStepFromStepWithDataSet(FunctionalStep step, String stepParamDataSetValue, String stepTargetDataSetValue) {
        return FunctionalStep.builder()
            .from(step)
            .overrideDataSetWith(
                Maps.of(
                    "step param", stepParamDataSetValue,
                    "step target", stepTargetDataSetValue
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

