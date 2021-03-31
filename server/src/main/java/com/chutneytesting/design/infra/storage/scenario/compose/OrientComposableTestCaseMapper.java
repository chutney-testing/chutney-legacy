package com.chutneytesting.design.infra.storage.scenario.compose;

import static com.chutneytesting.design.domain.scenario.compose.ComposableTestCaseRepository.COMPOSABLE_TESTCASE_REPOSITORY_SOURCE;
import static com.chutneytesting.design.infra.storage.scenario.compose.OrientComposableStepMapper.vertexToComposableStep;
import static java.time.Instant.now;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.ComposableScenario;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.design.infra.storage.scenario.compose.wrapper.TestCaseVertex;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.Date;

class OrientComposableTestCaseMapper {

    // SAVE
    static TestCaseVertex testCaseToVertex(final ComposableTestCase composableTestCase, OVertex dbTestCase) {
        return TestCaseVertex.builder()
            .from(dbTestCase)
            .withId(composableTestCase.id)
            .withTitle(composableTestCase.metadata.title())
            .withDescription(composableTestCase.metadata.description())
            .withCreationDate(Date.from(composableTestCase.metadata.creationDate()))
            .withTags(composableTestCase.metadata.tags())
            .withParameters(composableTestCase.composableScenario.parameters)
            .withDatasetId(composableTestCase.metadata.datasetId().orElse(null))
            .withUpdateDate(Date.from(now()))
            .withAuthor(composableTestCase.metadata.author())
            .withSteps(composableTestCase.composableScenario.composableSteps)
            .build();
    }

    // GET
    static ComposableTestCase vertexToTestCase(final TestCaseVertex testCaseVertex) {
        TestCaseMetadata metadata = TestCaseMetadataImpl.builder()
            .withId(testCaseVertex.id())
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
            .withComposableSteps(vertexToComposableStep(testCaseVertex.scenario()))
            .withParameters(testCaseVertex.parameters())
            .build();

        return new ComposableTestCase(
            testCaseVertex.id(),
            metadata,
            scenario
        );
    }

}
