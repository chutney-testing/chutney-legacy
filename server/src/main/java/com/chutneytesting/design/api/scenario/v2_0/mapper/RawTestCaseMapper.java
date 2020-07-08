package com.chutneytesting.design.api.scenario.v2_0.mapper;

import static org.hjson.JsonValue.readHjson;

import com.chutneytesting.design.api.compose.dto.KeyValue;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.RawTestCaseDto;
import com.chutneytesting.design.domain.scenario.ScenarioNotParsableException;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
import com.chutneytesting.execution.domain.compiler.GwtScenarioMarshaller;
import org.hjson.Stringify;

public class RawTestCaseMapper {

    private static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

    // RawTestCase -> GwtTestCase
    public static GwtTestCase fromRaw(RawTestCase testCase) {
        String jsonScenario = formatContentToJson(testCase.content);
        GwtScenario gwtScenario = marshaller.deserialize(testCase.metadata().title(), testCase.metadata().description(), jsonScenario);
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(testCase.metadata().id())
                .withTitle(testCase.metadata().title())
                .withDescription(testCase.metadata().description())
                .withTags(testCase.metadata().tags())
                .withCreationDate(testCase.metadata().creationDate())
                .withRepositorySource(testCase.metadata().repositorySource())
                .build())
            .withScenario(gwtScenario)
            .withDataSet(testCase.computedParameters())
            .build();
    }

    // DTO -> RawTestCase
    public static GwtTestCase fromDto(RawTestCaseDto dto) {
        String jsonScenario = formatContentToJson(dto.content());
        GwtScenario gwtScenario = marshaller.deserialize(dto.title(), dto.description().orElse(""), jsonScenario);
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(dto.id().orElse(null))
                .withTitle(dto.title())
                .withDescription(dto.description().orElse(null))
                .withTags(dto.tags())
                .withCreationDate(dto.creationDate())
                .withRepositorySource(null)
                .build())
            .withScenario(gwtScenario)
            .withDataSet(KeyValue.toMap(dto.computedParameters()))
            .build();
    }

    private static String formatContentToJson(String content) {
        try {
            return readHjson(content).toString();
        }
        catch (Exception e) {
            throw new ScenarioNotParsableException("Malformed json or hjson format. ", e);
        }
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
            .content(readHjson(testCase.content).toString(Stringify.HJSON))
            .tags(testCase.metadata().tags())
            .creationDate(testCase.metadata().creationDate())
            .computedParameters(KeyValue.fromMap(testCase.computedParameters()))
            .build();
    }

    public static RawTestCaseDto toDto(GwtTestCase testCase) {
        return ImmutableRawTestCaseDto.builder()
            .id(testCase.metadata().id())
            .title(testCase.metadata().title())
            .description(testCase.metadata().description())
            .content(readHjson(marshaller.serialize(testCase.scenario)).toString(Stringify.HJSON))
            .tags(testCase.metadata().tags())
            .creationDate(testCase.metadata().creationDate())
            .computedParameters(KeyValue.fromMap(testCase.dataSet))
            .build();
    }

}
