package com.chutneytesting.design.api.scenario.compose.mapper;

import static com.chutneytesting.design.domain.scenario.compose.ComposableTestCaseRepository.COMPOSABLE_TESTCASE_REPOSITORY_SOURCE;
import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;

import com.chutneytesting.design.api.scenario.compose.dto.ComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.tools.ui.KeyValue;
import com.chutneytesting.design.domain.scenario.compose.ComposableScenario;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.util.List;
import java.util.stream.Collectors;

public class ComposableTestCaseMapper {

    public ComposableTestCaseMapper() {
    }

    public static ComposableTestCase fromDto(ComposableTestCaseDto composableTestCaseDto) {
        return new ComposableTestCase(
            fromFrontId(composableTestCaseDto.id()),
            testCaseMetadataFromDto(composableTestCaseDto),
            fromComposableStepsDto(composableTestCaseDto)
        );
    }

    public static ComposableTestCaseDto toDto(ComposableTestCase composableTestCase) {
        return ImmutableComposableTestCaseDto.builder()
            .id(toFrontId(composableTestCase.id))
            .title(composableTestCase.metadata.title())
            .description(composableTestCase.metadata.description())
            .creationDate(composableTestCase.metadata.creationDate())
            .computedParameters(KeyValue.fromMap(composableTestCase.parameters))
            .scenario(
                ImmutableComposableScenarioDto.builder()
                    .addAllComponentSteps(toComposableStepsDto(composableTestCase))
                    .addAllParameters(KeyValue.fromMap(composableTestCase.composableScenario.parameters))
                    .build()
            )
            .tags(composableTestCase.metadata.tags())
            .datasetId(toFrontId(composableTestCase.metadata.datasetId().orElse("")))
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
            .withRepositorySource(COMPOSABLE_TESTCASE_REPOSITORY_SOURCE)
            .withTags(composableTestCaseDto.tags())
            .withDatasetId(fromFrontId(composableTestCaseDto.datasetId()))
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
