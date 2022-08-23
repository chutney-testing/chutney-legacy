package com.chutneytesting.component.scenario.api;

import com.chutneytesting.component.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.component.scenario.api.dto.ComposableStepDto;
import com.chutneytesting.component.scenario.api.dto.ComposableTestCaseDto;
import com.chutneytesting.component.scenario.api.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.component.scenario.api.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutableComposableTestCaseMapper {

    private ExecutableComposableTestCaseMapper() {
    }

    public static ComposableTestCaseDto toDto(ExecutableComposedTestCase composableTestCase) {
        return ImmutableComposableTestCaseDto.builder()
            .id(composableTestCase.id())
            .title(composableTestCase.metadata.title())
            .description(composableTestCase.metadata.description())
            .creationDate(composableTestCase.metadata.creationDate())
            .executionParameters(KeyValue.fromMap(composableTestCase.executionParameters))
            .scenario(
                ImmutableComposableScenarioDto.builder()
                    .addAllComponentSteps(toComposableStepsDto(composableTestCase))
                    .addAllParameters(KeyValue.fromMap(composableTestCase.composedScenario.parameters))
                    .build()
            )
            .tags(composableTestCase.metadata.tags())
            .datasetId(composableTestCase.metadata.datasetId().orElse(""))
            .author(composableTestCase.metadata.author())
            .updateDate(composableTestCase.metadata.updateDate())
            .version(composableTestCase.metadata.version())
            .build();
    }

    private static List<ComposableStepDto> toComposableStepsDto(ExecutableComposedTestCase composableTestCase) {
        return composableTestCase.composedScenario.composedSteps.stream()
            .map(ExecutableComposableStepMapper::toDto)
            .collect(Collectors.toList());
    }
}
