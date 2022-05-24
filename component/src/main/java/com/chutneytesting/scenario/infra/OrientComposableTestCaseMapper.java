package com.chutneytesting.scenario.infra;

import static com.chutneytesting.scenario.domain.ComposableTestCaseRepository.COMPOSABLE_TESTCASE_REPOSITORY_SOURCE;
import static java.time.Instant.now;

import com.chutneytesting.ComposableIdUtils;
import com.chutneytesting.scenario.domain.ComposableStep;
import com.chutneytesting.scenario.domain.TestCaseMetadata;
import com.chutneytesting.scenario.domain.TestCaseMetadataImpl;
import com.chutneytesting.scenario.domain.ComposableScenario;
import com.chutneytesting.scenario.domain.ComposableTestCase;
import com.chutneytesting.scenario.infra.wrapper.TestCaseVertex;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

class OrientComposableTestCaseMapper {

    // SAVE
    static TestCaseVertex testCaseToVertex(final ComposableTestCase composableTestCase, OVertex dbTestCase) {
        String internalId = ComposableIdUtils.toInternalId(composableTestCase.id);
        return TestCaseVertex.builder()
            .from(dbTestCase)
            .withId(internalId)
            .withTitle(composableTestCase.metadata.title())
            .withDescription(composableTestCase.metadata.description())
            .withCreationDate(Date.from(composableTestCase.metadata.creationDate()))
            .withTags(composableTestCase.metadata.tags())
            .withParameters(composableTestCase.composableScenario.parameters)
            .withDatasetId(composableTestCase.metadata.datasetId().orElse(null))
            .withUpdateDate(Date.from(now()))
            .withAuthor(composableTestCase.metadata.author())
            .withSteps(convertIds(composableTestCase.composableScenario.composableSteps))
            .build();
    }

    private static List<ComposableStep> convertIds(List<ComposableStep> composableSteps) {
        return composableSteps.stream().map(cs -> ComposableStep.builder()
            .from(cs)
            .withId(ComposableIdUtils.toInternalId(cs.id))
            .withSteps(convertIds(cs.steps))
            .withExecutionParameters(cs.executionParameters)
            .build()).collect(Collectors.toList());
    }

    // GET
    static ComposableTestCase vertexToTestCase(final TestCaseVertex testCaseVertex) {
        String externalId = ComposableIdUtils.toExternalId(testCaseVertex.id());
        TestCaseMetadata metadata = TestCaseMetadataImpl.builder()
            .withId(externalId)
            .withTitle(testCaseVertex.title())
            .withDescription(testCaseVertex.description())
            .withCreationDate(testCaseVertex.creationDate())
            .withRepositorySource(COMPOSABLE_TESTCASE_REPOSITORY_SOURCE)
            .withTags(testCaseVertex.tags())
            .withDatasetId(testCaseVertex.datasetId())
            .withUpdateDate(testCaseVertex.updateDate())
            .withAuthor(testCaseVertex.author())
            .withVersion(testCaseVertex.version())
            .build();

        ComposableScenario scenario = ComposableScenario.builder()
            .withComposableSteps(OrientComposableStepMapper.vertexToComposableStep(testCaseVertex.scenario()))
            .withParameters(testCaseVertex.parameters())
            .build();

        return new ComposableTestCase(
            externalId,
            metadata,
            scenario
        );
    }

}
