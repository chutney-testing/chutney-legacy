package com.chutneytesting.design.infra.storage.scenario.compose;

import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import org.springframework.stereotype.Component;

@Component
public class ExecutableComposedTestCaseMapper {

    private final ExecutableComposedStepMapper executableComposedStepMapper;

    public ExecutableComposedTestCaseMapper(ExecutableComposedStepMapper executableComposedStepMapper) {
        this.executableComposedStepMapper = executableComposedStepMapper;
    }

    public ExecutableComposedTestCase composableToExecutable(ComposableTestCase composableTestCase) {
        return new ExecutableComposedTestCase(
            composableTestCase.metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(executableComposedStepMapper.composableToExecutable(composableTestCase.composableScenario.composableSteps))
                .withParameters(composableTestCase.composableScenario.parameters)
                .build());
    }

}
