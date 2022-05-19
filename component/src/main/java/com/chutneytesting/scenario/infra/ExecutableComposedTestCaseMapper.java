package com.chutneytesting.scenario.infra;

import com.chutneytesting.scenario.domain.ComposableTestCase;
import com.chutneytesting.execution.domain.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.ExecutableComposedTestCase;
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
