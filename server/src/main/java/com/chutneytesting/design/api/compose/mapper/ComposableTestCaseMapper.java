package com.chutneytesting.design.api.compose.mapper;

import static com.chutneytesting.design.domain.compose.ComposableTestCaseRepository.COMPOSABLE_TESTCASE_REPOSITORY_SOURCE;

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
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

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

    public static String toFrontId(@NonNull String id) {
        if (isComposableTestCaseId(id)) {
            return id.replace("#", "").replace(":", "-");
        }
        return id;
    }

    public static String fromFrontId(String id) {
        return fromFrontId(Optional.of(id));
    }

    public static String fromFrontId(Optional<String> id) {
        return id.map(s -> {
            if (isComposableFrontId(s)) {
                return "#" + s.replace("-", ":");
            }
            return s;
        }).orElse("");
    }

    public static boolean isComposableFrontId(String frontId) {
        return frontId.contains("-");
    }

    public static boolean isComposableTestCaseId(String testCaseId) {
        return testCaseId.contains("#") && testCaseId.contains(":");
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
