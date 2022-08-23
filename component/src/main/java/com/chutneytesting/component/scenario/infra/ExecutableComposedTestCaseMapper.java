package com.chutneytesting.component.scenario.infra;

import com.chutneytesting.component.execution.domain.ExecutableComposedScenario;
import com.chutneytesting.component.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.component.scenario.domain.ComposableTestCase;
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
