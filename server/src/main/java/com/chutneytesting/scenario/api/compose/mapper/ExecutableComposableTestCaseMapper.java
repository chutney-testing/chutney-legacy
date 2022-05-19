package com.chutneytesting.scenario.api.compose.mapper;

import static com.chutneytesting.tools.orient.ComposableIdUtils.toFrontId;

import com.chutneytesting.scenario.api.compose.dto.ComposableStepDto;
import com.chutneytesting.scenario.api.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.scenario.api.compose.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.scenario.api.compose.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.tools.ui.KeyValue;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutableComposableTestCaseMapper {

    private ExecutableComposableTestCaseMapper() {
    }

    public static ComposableTestCaseDto toDto(ExecutableComposedTestCase composableTestCase) {
        return ImmutableComposableTestCaseDto.builder()
            .id(toFrontId(composableTestCase.id()))
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
            .datasetId(toFrontId(composableTestCase.metadata.datasetId().orElse("")))
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
