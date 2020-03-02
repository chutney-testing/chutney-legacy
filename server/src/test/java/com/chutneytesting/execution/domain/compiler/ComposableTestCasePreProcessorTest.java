package com.chutneytesting.execution.domain.compiler;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.Strategy;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ComposableTestCasePreProcessorTest {

    private ObjectMapper objectMapper = new WebConfiguration().objectMapper();

    private ComposableTestCasePreProcessor sut;

    private static final String VALUE = "value";
    private static final String PARAM_NAME = "param_name";
    private static final String GLOBAL_PARAMETER = "global.parameter";

    private static final String FIRST_ITERATION_VALUE = "iteration_1 ";
    private static final String SECOND_ITERATION_VALUE = "iteration_2";

    @Before
    public void setUp() {
        GlobalvarRepository globalvarRepository = Mockito.mock(GlobalvarRepository.class);
        when(globalvarRepository.getFlatMap())
            .thenReturn(singletonMap(GLOBAL_PARAMETER, "[{\"" + VALUE + "\":\"" + FIRST_ITERATION_VALUE + "\"}, {\"value\":\"" + SECOND_ITERATION_VALUE + "\"}]"));

        sut = new ComposableTestCasePreProcessor(objectMapper, globalvarRepository);
    }

    @Test
    public void should_replace_parameters_and_apply_strategy_when_strategy_input_is_also_a_parameter() {
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
        ComposableTestCase actual = sut.apply(composableTestase);

        // Then
        assertThat(actual.composableScenario.functionalSteps.size()).isEqualTo(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.size()).isEqualTo(2);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).name).isEqualTo("Iteration : " + FIRST_ITERATION_VALUE + " - iteration 1");
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(1).name).isEqualTo("Iteration : " + SECOND_ITERATION_VALUE + " - iteration 2");
    }
}

