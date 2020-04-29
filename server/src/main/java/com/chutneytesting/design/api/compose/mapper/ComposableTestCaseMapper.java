package com.chutneytesting.design.api.compose.mapper;

import static com.chutneytesting.design.domain.compose.ComposableTestCaseRepository.COMPOSABLE_TESTCASE_REPOSITORY_SOURCE;
import static com.chutneytesting.tools.ui.OrientUtils.fromFrontId;
import static com.chutneytesting.tools.ui.OrientUtils.toFrontId;

import com.chutneytesting.design.api.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.api.compose.dto.FunctionalStepDto;
import com.chutneytesting.design.api.compose.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.design.api.compose.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.design.api.compose.dto.KeyValue;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
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
            composableScenarioFromDto(composableTestCaseDto)
            );
    }

    public static ComposableTestCaseDto toDto(ComposableTestCase composableTestCase) {
        return ImmutableComposableTestCaseDto.builder()
            .id(toFrontId(composableTestCase.id))
            .title(composableTestCase.metadata.title())
            .description(composableTestCase.metadata.description())
            .creationDate(composableTestCase.metadata.creationDate())
            .dataSet(KeyValue.fromMap(composableTestCase.dataSet))
            .scenario(
                ImmutableComposableScenarioDto.builder()
                    .addAllComponentSteps(toComponentSteps(composableTestCase))
                    .addAllParameters(KeyValue.fromMap(composableTestCase.composableScenario.parameters))
                    .build()
            )
            .tags(composableTestCase.metadata.tags())
            .build();
    }

    private static TestCaseMetadata testCaseMetadataFromDto(ComposableTestCaseDto composableTestCaseDto) {
        return TestCaseMetadataImpl.builder()
            .withTitle(composableTestCaseDto.title())
            .withDescription(composableTestCaseDto.description().orElse(""))
            .withCreationDate(composableTestCaseDto.creationDate())
            .withRepositorySource(COMPOSABLE_TESTCASE_REPOSITORY_SOURCE)
            .withTags(composableTestCaseDto.tags())
            .build();
    }

    private static ComposableScenario composableScenarioFromDto(ComposableTestCaseDto composableTestCaseDto) {
        return ComposableScenario.builder()
            .withFunctionalSteps(
                composableTestCaseDto.scenario().componentSteps().stream()
                .map(FunctionalStepMapper::fromDto)
                .collect(Collectors.toList()))
            .withParameters(KeyValue.toMap(composableTestCaseDto.scenario().parameters()))
            .build();
    }

    private static List<FunctionalStepDto> toComponentSteps(ComposableTestCase composableTestCase) {
        return composableTestCase.composableScenario.functionalSteps.stream()
            .map(FunctionalStepMapper::toDto)
            .collect(Collectors.toList());
    }
}
