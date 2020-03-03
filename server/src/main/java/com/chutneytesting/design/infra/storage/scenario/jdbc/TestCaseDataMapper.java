package com.chutneytesting.design.infra.storage.scenario.jdbc;

import static com.chutneytesting.design.domain.scenario.TestCaseRepository.DEFAULT_REPOSITORY_SOURCE;

import com.chutneytesting.design.api.scenario.v2_0.mapper.GwtScenarioMapper;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
import com.chutneytesting.execution.domain.compiler.GwtScenarioMarshaller;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class TestCaseDataMapper { // TODO - test me more

    private static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

    public static TestCaseData toDto(GwtTestCase testCase) {
        return TestCaseData.builder()
            .withVersion("v2.1")
            .withId(testCase.metadata.id)
            .withTitle(testCase.metadata.title)
            .withCreationDate(testCase.metadata.creationDate)
            .withDescription(testCase.metadata.description)
            .withTags(testCase.metadata.tags)
            .withDataSet(testCase.dataSet)
            .withRawScenario(marshaller.serialize(testCase.scenario))
            .build();
    }

    public static TestCase fromDto(TestCaseData testCaseData) {
        switch (testCaseData.version) {
            case "v0.0": return fromV0_0(testCaseData);
            case "v1.0": return fromV1_0(testCaseData);
            case "v2.0": return fromV2_0(testCaseData);
            case "v2.1": return fromV2_1(testCaseData);
            case "git" : return fromGit();
            default:
                throw new RuntimeException("Cannot deserialize test case [" + testCaseData.id + "], unknown version [" + testCaseData.version + "]");
        }
    }

    private static TestCase fromV0_0(TestCaseData testCaseData) {
        return fromV1_0(testCaseData);
    }

    private static TestCase fromV1_0(TestCaseData testCaseData) {
        return RawTestCase.builder() // TODO - Rename RawTestCase into v1.0
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(testCaseData.id)
                .withTitle(testCaseData.title)
                .withDescription(testCaseData.description)
                .withCreationDate(testCaseData.creationDate)
                .withRepositorySource(DEFAULT_REPOSITORY_SOURCE)
                .withTags(testCaseData.tags)
                .build())
            .withDataSet(testCaseData.dataSet)
            .withScenario(testCaseData.rawScenario)
            .build();
    }

    private static TestCase fromV2_0(TestCaseData testCaseData) {
        return fromV2_1(testCaseData);
    }

    private static TestCase fromV2_1(TestCaseData dto) {
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(dto.id)
                .withTitle(dto.title)
                .withDescription(dto.description)
                .withCreationDate(dto.creationDate)
                .withRepositorySource(DEFAULT_REPOSITORY_SOURCE)
                .withTags(dto.tags)
                .build())
            .withDataSet(dto.dataSet)
            .withScenario(marshaller.deserialize(dto.title, dto.description, dto.rawScenario))
            .build();
    }

    private static TestCase fromGit() {
        throw new NotImplementedException();
    }

}
