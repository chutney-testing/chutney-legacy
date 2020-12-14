package com.chutneytesting.design.infra.storage.scenario.compose;

import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.StepImplementation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ExecutableComposedStepMapper {

    private final RawImplementationMapper rawImplementationMapper;

    public ExecutableComposedStepMapper(RawImplementationMapper rawImplementationMapper) {
        this.rawImplementationMapper = rawImplementationMapper;
    }

    List<ExecutableComposedStep> composableToExecutable(List<ComposableStep> composableSteps) {
        return composableSteps.stream()
            .map(this::composableToExecutable)
            .collect(Collectors.toList());
    }

    ExecutableComposedStep composableToExecutable(ComposableStep fs) {
        return ExecutableComposedStep.builder()
            .withName(fs.name)
            .withStrategy(fs.strategy)
            .withSteps(composableToExecutable(fs.steps))
            .withImplementation(toStepImplementation(fs.implementation))
            .withParameters(fs.builtInParameters)
            .withDataset(fs.enclosedUsageParameters)
            .build();
    }

    private Optional<StepImplementation> toStepImplementation(Optional<String> rawImplementation) {
        return rawImplementation.map(rawImplementationMapper::deserialize);
    }

}
