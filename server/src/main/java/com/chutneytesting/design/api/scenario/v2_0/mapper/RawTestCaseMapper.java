package com.chutneytesting.design.api.scenario.v2_0.mapper;


import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.RawTestCaseDto;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
import com.chutneytesting.execution.domain.compiler.GwtScenarioMarshaller;
import com.chutneytesting.tools.ui.KeyValue;

public class RawTestCaseMapper {

    private static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

    // RawTestCase -> GwtTestCase
    public static GwtTestCase fromRaw(RawTestCase testCase) {
        GwtScenario gwtScenario = marshaller.deserializeFromYaml(testCase.metadata().title(), testCase.metadata().description(), testCase.scenario);
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(testCase.metadata().id())
                .withTitle(testCase.metadata().title())
                .withDescription(testCase.metadata().description())
                .withTags(testCase.metadata().tags())
                .withCreationDate(testCase.metadata().creationDate())
                .withRepositorySource(testCase.metadata().repositorySource())
                .withAuthor(testCase.metadata.author)
                .withUpdateDate(testCase.metadata.updateDate)
                .withVersion(testCase.metadata.version)
                .build())
            .withScenario(gwtScenario)
            .withExecutionParameters(testCase.executionParameters())
            .build();
    }

    // DTO -> RawTestCase
    public static GwtTestCase fromDto(RawTestCaseDto dto) {
        GwtScenario gwtScenario = marshaller.deserializeFromYaml(dto.title(), dto.description().orElse(""), dto.scenario());
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(dto.id().orElse(null))
                .withTitle(dto.title())
                .withDescription(dto.description().orElse(null))
                .withTags(dto.tags())
                .withCreationDate(dto.creationDate())
                .withRepositorySource(null)
                .withAuthor(dto.author())
                .withUpdateDate(dto.updateDate())
                .withVersion(dto.version())
                .build())
            .withScenario(gwtScenario)
            .withExecutionParameters(KeyValue.toMap(dto.parameters()))
            .build();
    }

    public static RawTestCaseDto toDto(TestCase testCase) {
        if (testCase instanceof RawTestCase) {
            return toDto((RawTestCase) testCase);
        }

        if (testCase instanceof GwtTestCase) {
            return toDto((GwtTestCase) testCase);
        }

        throw new IllegalStateException("Bad format." +
            "Test Case [" + testCase.metadata().id() + "] is not a RawTestCase but a " + testCase.getClass().getSimpleName());
    }

    public static RawTestCaseDto toDto(RawTestCase testCase) {
        return ImmutableRawTestCaseDto.builder()
            .id(testCase.metadata().id())
            .title(testCase.metadata().title())
            .description(testCase.metadata().description())
            .scenario(testCase.scenario)
            .tags(testCase.metadata().tags())
            .creationDate(testCase.metadata().creationDate())
            .parameters(KeyValue.fromMap(testCase.executionParameters()))
            .author(testCase.metadata.author)
            .updateDate(testCase.metadata.updateDate)
            .version(testCase.metadata.version)
            .build();
    }

    public static RawTestCaseDto toDto(GwtTestCase testCase) {
        return ImmutableRawTestCaseDto.builder()
            .id(testCase.metadata().id())
            .title(testCase.metadata().title())
            .description(testCase.metadata().description())
            .scenario(marshaller.serializeToYaml(testCase.scenario))
            .tags(testCase.metadata().tags())
            .creationDate(testCase.metadata().creationDate())
            .parameters(KeyValue.fromMap(testCase.executionParameters))
            .author(testCase.metadata.author)
            .updateDate(testCase.metadata.updateDate)
            .version(testCase.metadata.version)
            .build();
    }

}
