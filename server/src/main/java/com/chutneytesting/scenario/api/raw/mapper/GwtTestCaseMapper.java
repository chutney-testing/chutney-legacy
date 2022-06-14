package com.chutneytesting.scenario.api.raw.mapper;

import com.chutneytesting.scenario.api.raw.dto.GwtTestCaseDto;
import com.chutneytesting.scenario.api.raw.dto.ImmutableGwtTestCaseDto;
import com.chutneytesting.scenario.domain.TestCase;
import com.chutneytesting.scenario.domain.TestCaseMetadataImpl;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.domain.raw.RawTestCase;
import java.util.Collections;

// TODO test me
public class GwtTestCaseMapper {

    // DTO -> TestCase
    public static GwtTestCase fromDto(GwtTestCaseDto dto) {
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(dto.id().orElse(null))
                .withTitle(dto.title())
                .withDescription(dto.description().orElse(null))
                .withTags(dto.tags())
                .withCreationDate(dto.creationDate().orElse(null))
                .withAuthor(dto.author())
                .withUpdateDate(dto.updateDate())
                .withVersion(dto.version())
                .build())
            .withScenario(GwtScenarioMapper.fromDto(dto.title(), dto.description().orElse(""), dto.scenario()))
            .withExecutionParameters(dto.executionParameters())
            .build();
    }

    // TestCase -> DTO
    public static GwtTestCaseDto toDto(TestCase testCase) {
        if (testCase instanceof GwtTestCase) {
            return fromGwt((GwtTestCase) testCase);
        }

        if (testCase instanceof RawTestCase) {
            return fromGwt(RawTestCaseMapper.fromRaw((RawTestCase) testCase));
        }

        throw new IllegalStateException("Bad format. " +
            "Test Case [" + testCase.metadata().id() + "] is not a GwtTestCase but a " + testCase.getClass().getSimpleName());
    }

    private static GwtTestCaseDto fromGwt(GwtTestCase testCase) {
        return ImmutableGwtTestCaseDto.builder()
            .id(testCase.metadata().id())
            .title(testCase.metadata().title())
            .description(testCase.metadata().description())
            .tags(testCase.metadata().tags())
            .executions(Collections.emptyList())
            .creationDate(testCase.metadata().creationDate())
            .scenario(GwtScenarioMapper.toDto(testCase.scenario))
            .executionParameters(testCase.executionParameters)
            .author(testCase.metadata.author)
            .updateDate(testCase.metadata.updateDate)
            .version(testCase.metadata.version)
            .build();
    }

}
