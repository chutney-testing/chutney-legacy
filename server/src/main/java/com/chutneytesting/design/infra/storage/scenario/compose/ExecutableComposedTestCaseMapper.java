package com.chutneytesting.design.infra.storage.scenario.compose;

import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import org.springframework.stereotype.Component;

@Component
public class ExecutableComposedTestCaseMapper {

    public ExecutableComposedTestCase map(ComposableTestCase composableTestCase) {
        return new ExecutableComposedTestCase(
            composableTestCase.id,
            composableTestCase.metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(map(composableTestCase.composableScenario.composableSteps))
                .withParameters(composableTestCase.composableScenario.parameters)
                .build());
    }

}
