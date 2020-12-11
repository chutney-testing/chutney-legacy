package com.chutneytesting.design.api.scenario.v2_0.mapper;

import com.chutneytesting.design.api.scenario.v2_0.dto.GwtTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtTestCaseDto;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
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
                .withRepositorySource(dto.repositorySource().orElse(null))
                .withAuthor(dto.author())
                .withUpdateDate(dto.updateDate())
                .withVersion(dto.version())
                .build())
            .withScenario(GwtScenarioMapper.fromDto(dto.title(), dto.description().orElse(""), dto.scenario()))
            .withParameters(dto.parameters())
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
            .repositorySource(testCase.metadata().repositorySource())
            .tags(testCase.metadata().tags())
            .executions(Collections.emptyList())
            .creationDate(testCase.metadata().creationDate())
            .scenario(GwtScenarioMapper.toDto(testCase.scenario))
            .parameters(testCase.parameters)
            .author(testCase.metadata.author)
            .updateDate(testCase.metadata.updateDate)
            .version(testCase.metadata.version)
            .build();
    }

}
