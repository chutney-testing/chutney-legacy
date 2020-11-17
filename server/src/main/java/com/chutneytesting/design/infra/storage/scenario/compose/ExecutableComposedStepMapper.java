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

    List<ExecutableComposedStep> composableToExecutable(List<ComposableStep> composableSteps) {
        return composableSteps.stream()
            .map(OrientComposableStepMapper::composableToExecutable)
            .collect(Collectors.toList());
    }

    ExecutableComposedStep map(ComposableStep fs) {
        return ExecutableComposedStep.builder()
            .withName(fs.name)
            .withStrategy(fs.strategy)
            .withSteps(composableToExecutable(fs.steps))
            .withImplementation(toStepImplementation(fs.implementation.orElse("")))
            .withParameters(fs.parameters)
            .overrideDataSetWith(fs.dataSet)
            .build();
    }

    Optional<StepImplementation> toStepImplementation(String implementation) {
        RawImplementation.builder().from(implementation).build();
        new StepImplementation();
        return RawImplementationMapper.builder().(
            composedStep.name,
            toDto(findTargetByName(composedStep.stepImplementation.map(ComposableImplementation::targetName).orElse(""), env)),
            this.mapStrategy(composedStep.strategy),
            implementation.map(ComposableImplementation::type).orElse(""),
            implementation.map(ComposableImplementation::inputs).orElse(emptyMap()),
            composedStep.steps.stream().map(f -> convert(f, env)).collect(Collectors.toList()),
            implementation.map(ComposableImplementation::outputs).orElse(emptyMap()),
            env
        );
    }

}
