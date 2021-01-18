package com.chutneytesting.design.infra.storage.scenario.compose;

import static com.chutneytesting.design.domain.scenario.compose.ComposableTestCaseRepository.COMPOSABLE_TESTCASE_REPOSITORY_SOURCE;
import static com.chutneytesting.design.infra.storage.scenario.compose.OrientComposableStepMapper.buildComposableStepsChildren;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_AUTHOR;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_DATASET_ID;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_DESCRIPTION;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TITLE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_UPDATEDATE;
import static java.time.Instant.now;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.ComposableScenario;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.design.infra.storage.scenario.compose.dto.TestCaseVertex;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.Date;
import java.util.List;
import java.util.Map;

class OrientComposableTestCaseMapper {

    // SAVE
    static TestCaseVertex testCaseToVertex(final ComposableTestCase composableTestCase, OVertex dbTestCase, ODatabaseSession dbSession) {
        return TestCaseVertex.builder()
            .from(dbTestCase)
            .usingSession(dbSession)
            .withTitle(composableTestCase.metadata.title())
            .withDescription(composableTestCase.metadata.description())
            .withCreationDate(Date.from(composableTestCase.metadata.creationDate()))
            .withTags(composableTestCase.metadata.tags())
            .withParameters(composableTestCase.composableScenario.parameters)
            .withDatasetId(composableTestCase.metadata.datasetId())
            .withUpdateDate(Date.from(now()))
            .withAuthor(composableTestCase.metadata.author())
            .withSteps(composableTestCase.composableScenario.composableSteps)
            .build();
    }

    // GET
    static ComposableTestCase vertexToTestCase(final OVertex dbTestCase, ODatabaseSession dbSession) {
        TestCaseMetadata metadata = TestCaseMetadataImpl.builder()
            .withId(dbTestCase.getIdentity().toString())
            .withTitle(dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_TITLE))
            .withDescription(dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_DESCRIPTION))
            .withCreationDate(((Date) dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_CREATIONDATE)).toInstant())
            .withRepositorySource(COMPOSABLE_TESTCASE_REPOSITORY_SOURCE)
            .withTags(dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_TAGS))
            .withDatasetId(dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_DATASET_ID))
            .withUpdateDate(((Date) dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_UPDATEDATE)).toInstant())
            .withAuthor(dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_AUTHOR))
            .withVersion(dbTestCase.getVersion())
            .build();

        List<ComposableStep> composableStepRefs = buildComposableStepsChildren(dbTestCase);

        Map<String, String> parameters = dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_PARAMETERS);

        return new ComposableTestCase(
            dbTestCase.getIdentity().toString(),
            metadata,
            ComposableScenario.builder()
                .withComposableSteps(composableStepRefs)
                .withParameters(parameters)
                .build());
    }

}
