package com.chutneytesting.scenario.api;

import com.chutneytesting.scenario.api.dto.ComposableStepDto;
import com.chutneytesting.scenario.api.dto.ComposableTestCaseDto;
import com.chutneytesting.scenario.api.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.scenario.api.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.scenario.domain.ComposableScenario;
import com.chutneytesting.scenario.domain.ComposableTestCase;
import com.chutneytesting.scenario.domain.TestCaseMetadata;
import com.chutneytesting.scenario.domain.TestCaseMetadataImpl;
import com.chutneytesting.tools.ui.KeyValue;
import java.util.List;
import java.util.stream.Collectors;

public class ComposableTestCaseMapper {

    private ComposableTestCaseMapper() {
    }

    public static ComposableTestCase fromDto(ComposableTestCaseDto composableTestCaseDto) {
        return new ComposableTestCase(
            composableTestCaseDto.id().orElse(""),
            testCaseMetadataFromDto(composableTestCaseDto),
            fromComposableStepsDto(composableTestCaseDto)
        );
    }

    public static ComposableTestCaseDto toDto(ComposableTestCase composableTestCase) {
        return ImmutableComposableTestCaseDto.builder()
            .id(composableTestCase.id)
            .title(composableTestCase.metadata.title())
            .description(composableTestCase.metadata.description())
            .creationDate(composableTestCase.metadata.creationDate())
            .executionParameters(KeyValue.fromMap(composableTestCase.executionParameters))
            .scenario(
                ImmutableComposableScenarioDto.builder()
                    .addAllComponentSteps(toComposableStepsDto(composableTestCase))
                    .addAllParameters(KeyValue.fromMap(composableTestCase.composableScenario.parameters))
                    .build()
            )
            .tags(composableTestCase.metadata.tags())
            .datasetId(composableTestCase.metadata.datasetId().orElse(""))
            .author(composableTestCase.metadata.author())
            .updateDate(composableTestCase.metadata.updateDate())
            .version(composableTestCase.metadata.version())
            .build();
    }

    private static TestCaseMetadata testCaseMetadataFromDto(ComposableTestCaseDto composableTestCaseDto) {
        return TestCaseMetadataImpl.builder()
            .withTitle(composableTestCaseDto.title())
            .withDescription(composableTestCaseDto.description().orElse(""))
            .withCreationDate(composableTestCaseDto.creationDate())
            .withTags(composableTestCaseDto.tags())
            .withDatasetId(composableTestCaseDto.datasetId().orElse(""))
            .withAuthor(composableTestCaseDto.author())
            .withUpdateDate(composableTestCaseDto.updateDate())
            .withVersion(composableTestCaseDto.version())
            .build();
    }

    private static ComposableScenario fromComposableStepsDto(ComposableTestCaseDto composableTestCaseDto) {
        return ComposableScenario.builder()
            .withComposableSteps(
                composableTestCaseDto.scenario().componentSteps().stream()
                    .map(ComposableStepMapper::fromDto)
                    .collect(Collectors.toList()))
            .withParameters(KeyValue.toMap(composableTestCaseDto.scenario().parameters()))
            .build();
    }

    private static List<ComposableStepDto> toComposableStepsDto(ComposableTestCase composableTestCase) {
        return composableTestCase.composableScenario.composableSteps.stream()
            .map(ComposableStepMapper::toDto)
            .collect(Collectors.toList());
    }
}
