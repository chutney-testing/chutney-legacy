package com.chutneytesting.execution.domain.compiler;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.api.scenario.v2_0.mapper.GwtScenarioMapper;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtStep;
import com.chutneytesting.design.domain.scenario.gwt.GwtStepImplementation;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.domain.scenario.gwt.Strategy;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
import com.chutneytesting.execution.domain.ExecutionRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DataSetPreProcessorTest {

    private GlobalvarRepository globalvarRepository;

    @BeforeEach
    public void setUp() {
        globalvarRepository = Mockito.mock(GlobalvarRepository.class);
        Map<String, String> map = new HashMap<>();
        map.put("key.1", "value1");
        map.put("key.2", "value2");
        Mockito.when(globalvarRepository.getFlatMap()).thenReturn(map);
    }

    @Test
    public void should_replace_raw_scenario_parameters_with_data_set_values() {
        // Given
        RawDataSetPreProcessor dataSetPreProcessor = new RawDataSetPreProcessor(globalvarRepository);
        RawTestCase fakeTestCase = RawTestCase.builder()
            .withScenario("a blabla step with **aKey** and **anotherKey** and **key.1**")
            .withParameters(Maps.of("aKey", "a value", "anotherKey", "another value"))
            .build();

        // When
        RawTestCase actual = dataSetPreProcessor.apply(
            new ExecutionRequest(fakeTestCase, null, "")
        );

        // Then
        String expectedContent = "a blabla step with a value and another value and value1";
        assertThat(actual.scenario).isEqualToIgnoringCase(expectedContent);
    }

    @Test
    public void should_replace_gwt_scenario_parameters_with_data_set_values() {

        // Given
        Instant creation_date = Instant.now();
        String expected_title = "**titre**";
        String expected_description = "**description**";
        String expected_when_step = "when step";
        String expected_strategy_type = "StrategyType";
        Map<String, Object> expected_strategy_params = Maps.of("aParam", "a value", "another value", "AnotherParamValue");
        String expected_then_step = "then step with a value and value2";
        String expected_implementation = "another value";

        GwtTestCase parameterizedTestCase = GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withCreationDate(creation_date)
                .withTitle(expected_title)
                .withDescription(expected_description)
                .build())
            .withParameters(Maps.of("aKey", "a value", "anotherKey", "another value", "titre", "newTitle", "description", "newDesc", "type", expected_strategy_type))
            .withScenario(GwtScenario.builder()
                .withWhen(
                    GwtStep.builder()
                        .withDescription(expected_when_step)
                        .withStrategy(new Strategy("**type**", Maps.of("aParam", "**aKey**", "**anotherKey**", "AnotherParamValue"))).build()
                )
                .withThens(singletonList(
                    GwtStep.builder().withDescription("then step with **aKey** and **key.2**").withSubSteps(
                        GwtStep.builder().withDescription("then 3.1 step")
                            .withImplementation(new GwtStepImplementation("**anotherKey**", "", null, null, null)).build()).build())).build()).build();

        GwtDataSetPreProcessor dataSetPreProcessor = new GwtDataSetPreProcessor(new GwtScenarioMapper(), globalvarRepository);

        // When
        GwtTestCase actual = dataSetPreProcessor.apply(
            new ExecutionRequest(parameterizedTestCase, null, "")
        );

        // Then
        GwtTestCase evaluatedTestCase = GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withCreationDate(creation_date)
                .withTitle(expected_title)
                .withDescription(expected_description)
                .build())
            .withParameters(Maps.of("aKey", "a value", "anotherKey", "another value", "titre", "newTitle", "description", "newDesc", "type", expected_strategy_type))
            .withScenario(GwtScenario.builder()
                .withWhen(
                    GwtStep.builder()
                        .withDescription(expected_when_step)
                        .withStrategy(new Strategy(expected_strategy_type, expected_strategy_params)).build()
                )
                .withThens(singletonList(
                    GwtStep.builder().withDescription(expected_then_step).withSubSteps(
                        GwtStep.builder().withDescription("then 3.1 step")
                            .withImplementation(new GwtStepImplementation(expected_implementation, "", null, null, null)).build()).build())).build()).build();

        // Test case title and description are not evaluated by the DataSetPreProcessor even if data set contains matching keys
        assertThat(actual.metadata.title).isEqualTo(expected_title);
        assertThat(actual.metadata.description).isEqualTo(expected_description);

        assertThat(actual.scenario.when.description).isEqualTo(expected_when_step);
        assertThat(actual.scenario.when.strategy.map(s -> s.type).orElse("")).isEqualTo(expected_strategy_type);
        assertThat(actual.scenario.when.strategy.map(s -> s.parameters).orElse(emptyMap())).isEqualTo(expected_strategy_params);
        assertThat(actual.scenario.thens.get(0).description).isEqualTo(expected_then_step);
        assertThat(actual.scenario.thens.get(0).subSteps.get(0).implementation.map(i -> i.type).orElse("")).isEqualTo(expected_implementation);

        assertThat(actual).isEqualTo(evaluatedTestCase);
    }

}
