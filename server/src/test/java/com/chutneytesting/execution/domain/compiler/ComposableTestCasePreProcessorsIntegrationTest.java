package com.chutneytesting.execution.domain.compiler;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.Strategy;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.groovy.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComposableTestCasePreProcessorsIntegrationTest {

    private GlobalvarRepository globalvarRepository;
    private ObjectMapper objectMapper = new WebConfiguration().objectMapper();

    private LegacyComposableTestCasePreProcessor legacy;
    private ComposableTestCasePreProcessor sut;

    @Before
    public void setUp() {
        globalvarRepository = Mockito.mock(GlobalvarRepository.class);
        Map<String, String> map = new HashMap<>();
        map.put("key.1", "value1");
        map.put("key.2", "value2");
        Mockito.when(globalvarRepository.getFlatMap()).thenReturn(map);

        sut = new ComposableTestCasePreProcessor(objectMapper, globalvarRepository);

        legacy = new LegacyComposableTestCasePreProcessor(globalvarRepository, objectMapper);
    }

    @Test
    public void should_replace_composableTestCase_scenario_parameters_with_scoped_data_set_values() {
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
        final ComposableTestCase composableTestCaseProcessed = sut.apply(composableTestCase);
        final ComposableTestCase nugget = legacy.apply(composableTestCase);

        assertThat(composableTestCaseProcessed).isEqualToComparingFieldByFieldRecursively(nugget);

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
            .withImplementation(ofNullable(actionImplementation))
            .build();

        FunctionalStep step = FunctionalStep.builder()
            .withId("2")
            .withName(stepName)
            .withSteps(Arrays.asList(buildStepFromActionWithDataSet(action, "**target**")))
            .withStrategy(new Strategy("Loop", Collections.singletonMap("data", loopData)))
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
                .withDescription(testCaseDescription)
                .build(),
            ComposableScenario.builder()
                .withFunctionalSteps(
                    Arrays.asList(
                        buildStepFromStepWithDataSet(step, "**testcase param**", "", "hard testcase target"),
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

        final ComposableTestCase composableTestCaseProcessed = sut.apply(composableTestCase);
        //final ComposableTestCase nugget = legacy.apply(composableTestCase);
        //Assumptions.assumeThat(composableTestCaseProcessed).isEqualToComparingFieldByFieldRecursively(nugget);

        // Then
        assertThat(composableTestCaseProcessed.id()).isEqualTo(composableTestCase.id());
        assertThat(composableTestCaseProcessed.metadata.title()).isEqualTo(format(testCaseTitle, dataSet.get("testcase title")));
        assertThat(composableTestCaseProcessed.metadata.description()).isEqualTo(testCaseDescription);

        /* Step 1 */
        FunctionalStep step_1 = composableTestCaseProcessed.composableScenario.functionalSteps.get(0);
        assertThat(step_1.steps.size()).isEqualTo(2);
        assertThat(step_1.strategy).isEqualTo(Strategy.DEFAULT);
        assertThat(step_1.dataSet.size()).isEqualTo(4);
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
        assertThat(step_2.dataSet.size()).isEqualTo(4);
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

    private class LegacyComposableTestCasePreProcessor implements TestCasePreProcessor<ComposableTestCase> {

        private final Logger LOGGER = LoggerFactory.getLogger(LegacyComposableTestCasePreProcessor.class);

        private GlobalvarRepository globalvarRepository;
        private ObjectMapper objectMapper;

        public LegacyComposableTestCasePreProcessor(GlobalvarRepository globalvarRepository, ObjectMapper objectMapper) {
            this.globalvarRepository = globalvarRepository;
            this.objectMapper = objectMapper;
        }

        @Override
        public int order() {
            return 10;
        }

        @Override
        public ComposableTestCase apply(ComposableTestCase testCase) {
            Map<String, String> globalVariable = globalvarRepository.getFlatMap();
            return new ComposableTestCase(
                testCase.id,
                applyToMetadata(testCase.metadata, testCase.dataSet, globalVariable),
                applyToScenario(testCase.composableScenario, testCase.dataSet, globalVariable),
                testCase.dataSet);
        }

        private TestCaseMetadata applyToMetadata(TestCaseMetadata metadata, Map<String, String> dataSet, Map<String, String> globalVariable) {
            return TestCaseMetadataImpl.TestCaseMetadataBuilder
                .from(metadata)
                .withTitle(replaceParams(metadata.title(), globalVariable, dataSet))
                .withDescription(replaceParams(metadata.description(), globalVariable, dataSet))
                .build();
        }

        private ComposableScenario applyToScenario(ComposableScenario composableScenario, Map<String, String> dataSet, Map<String, String> globalVariable) {
            return ComposableScenario.builder()
                .withFunctionalSteps(
                    composableScenario.functionalSteps.stream()
                        .map(step -> applyToFunctionalStep(step, dataSet, globalVariable))
                        .collect(Collectors.toList())
                )
                .withParameters(composableScenario.parameters)
                .build();
        }

        private FunctionalStep applyToFunctionalStep(FunctionalStep functionalStep, Map<String, String> dataSet, Map<String, String> globalVariable) {
            Map<String, String> scopedDataSet = applyToDataSet(functionalStep.dataSet, dataSet, globalVariable);
            List<FunctionalStep> subSteps = functionalStep.steps;
            boolean hasLoopStrategy = "Loop".equals(functionalStep.strategy.type);

            // Generate steps for loop strategy
            if (hasLoopStrategy) {
                subSteps = generateIterationsSteps(functionalStep, scopedDataSet, globalVariable);
            }

            // Preprocess substeps - Recurse
            FunctionalStep.FunctionalStepBuilder parentStepBuilder = FunctionalStep.builder()
                .withName(replaceParams(functionalStep.name, globalvarRepository.getFlatMap(), scopedDataSet))
                .withSteps(
                    subSteps.stream()
                        .map(f -> applyToFunctionalStep(f, scopedDataSet, globalVariable))
                        .collect(Collectors.toList())
                )
                .withImplementation(functionalStep.implementation.map(v -> replaceParams(v, globalvarRepository.getFlatMap(), scopedDataSet)));

            // Set scoped dataset
            if (!hasLoopStrategy) {
                parentStepBuilder
                    .withStrategy(functionalStep.strategy)
                    .overrideDataSetWith(scopedDataSet);
            }

            return parentStepBuilder.build();
        }

        private Map<String, String> applyToDataSet(Map<String, String> dataSet, Map<String, String> dataSetToApply, Map<String, String> globalVariables) {
            HashMap<String, String> scopeDataSet = new HashMap<>();
            Map<Boolean, List<Map.Entry<String, String>>> splitDataSet = dataSet.entrySet().stream().collect(Collectors.groupingBy(o -> isBlank(o.getValue())));

            Optional.ofNullable(splitDataSet.get(true))
                .ifPresent(l -> l.forEach(e -> scopeDataSet.put(e.getKey(), dataSetToApply.get(e.getKey()))));
            Optional.ofNullable(splitDataSet.get(false))
                .ifPresent(l -> l.forEach(e -> scopeDataSet.put(e.getKey(), replaceParams(e.getValue(), globalVariables, dataSetToApply))));

            return scopeDataSet;
        }

        private List<FunctionalStep> generateIterationsSteps(FunctionalStep functionalStep, Map<String, String> scopedDataSet, Map<String, String> globalVariable) {
            List<FunctionalStep> substeps = new ArrayList<>();
            JsonNode iterations;

            try {
                String data = (String) functionalStep.strategy.parameters.get("data");
                iterations = objectMapper.readTree(StringEscapeUtils.unescapeJson(replaceParams(data, globalVariable, scopedDataSet)));
            } catch (IOException e) {
                LOGGER.error("Error reading json loop data", e);
                iterations = NullNode.getInstance();
            }
            for (int i = 0; i < iterations.size(); i++) {
                Map<String, String> dataset = new HashMap<>(scopedDataSet);
                JsonNode data;

                if (iterations.isArray()) {
                    data = iterations.get(i);
                } else {
                    data = iterations;
                }

                scopedDataSet.forEach((k, v) -> {
                    if (data.has(k)) {
                        dataset.put(k, data.get(k).textValue());
                    }
                });

                substeps.add(FunctionalStep.builder()
                    .withName(replaceParams(functionalStep.name, globalVariable, scopedDataSet) + " - iteration " + (i + 1))
                    .withSteps(replaceSubStepsParams(functionalStep, data, globalVariable))
                    .withImplementation(functionalStep.implementation.map(v -> replaceParams(v, globalVariable, scopedDataSet)))
                    .overrideDataSetWith(dataset)
                    .build());
            }

            return substeps;
        }

        private List<FunctionalStep> replaceSubStepsParams(FunctionalStep functionalStep, JsonNode data, Map<String, String> globalVariable) {
            List<FunctionalStep> steps = new ArrayList<>();

            functionalStep.steps.forEach(subStep -> {
                    Map<String, String> scopedDataSet = new HashMap<>();

                    subStep.dataSet.forEach((key, value) -> {
                        if (data.has(key))
                            scopedDataSet.put(key, data.get(key).textValue());
                    });

                    steps.add(FunctionalStep.builder()
                        .withName(replaceParams(subStep.name, globalVariable, scopedDataSet))
                        .withSteps(subStep.steps)
                        .withImplementation(subStep.implementation.map(v -> replaceParams(v, globalVariable, scopedDataSet)))
                        .withStrategy(subStep.strategy)
                        .overrideDataSetWith(scopedDataSet)
                        .build());
                }
            );

            return steps;

        }
    }

}
