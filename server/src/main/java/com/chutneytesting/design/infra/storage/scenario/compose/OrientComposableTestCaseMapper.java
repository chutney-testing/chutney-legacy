package com.chutneytesting.design.infra.storage.scenario.compose;

import static com.chutneytesting.design.domain.scenario.compose.ComposableTestCaseRepository.COMPOSABLE_TESTCASE_REPOSITORY_SOURCE;
import static com.chutneytesting.design.infra.storage.scenario.compose.OrientComposableStepMapper.buildComposableStepsChildren;
import static com.chutneytesting.design.infra.storage.scenario.compose.OrientComposableStepMapper.setSubStepReferences;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_AUTHOR;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_DATASET_ID;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_DESCRIPTION;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TITLE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_UPDATEDATE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.setOnlyOnceProperty;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.setOrRemoveProperty;
import static java.time.Instant.now;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.ComposableScenario;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.design.infra.storage.scenario.compose.dto.StepVertex;
import com.chutneytesting.security.domain.User;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.Date;
import java.util.List;
import java.util.Map;

class OrientComposableTestCaseMapper {

    // SAVE
    static void testCaseToVertex(final ComposableTestCase composableTestCase, OVertex dbTestCase, ODatabaseSession dbSession) {
        StepVertex stepVertex = StepVertex.builder().from(dbTestCase).build();

        dbTestCase.setProperty(TESTCASE_CLASS_PROPERTY_TITLE, composableTestCase.metadata.title(), OType.STRING);
        setOrRemoveProperty(dbTestCase, TESTCASE_CLASS_PROPERTY_DESCRIPTION, composableTestCase.metadata.description(), OType.STRING);
        setOnlyOnceProperty(dbTestCase, TESTCASE_CLASS_PROPERTY_CREATIONDATE, Date.from(composableTestCase.metadata.creationDate()), OType.DATETIME);
        dbTestCase.setProperty(TESTCASE_CLASS_PROPERTY_TAGS, composableTestCase.metadata.tags(), OType.EMBEDDEDLIST);
        setOrRemoveProperty(dbTestCase, TESTCASE_CLASS_PROPERTY_PARAMETERS, composableTestCase.composableScenario.parameters, OType.EMBEDDEDMAP);
        setOrRemoveProperty(dbTestCase, TESTCASE_CLASS_PROPERTY_DATASET_ID, composableTestCase.metadata.datasetId().orElse(null), OType.STRING);
        dbTestCase.setProperty(TESTCASE_CLASS_PROPERTY_UPDATEDATE, Date.from(now()), OType.DATETIME);
        setOrRemoveProperty(dbTestCase, TESTCASE_CLASS_PROPERTY_AUTHOR, composableTestCase.metadata.author(), a -> !User.isAnonymous(a), OType.STRING);

        setSubStepReferences(stepVertex, composableTestCase.composableScenario.composableSteps, dbSession);
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

        List<ComposableStep> composableStepRefs = buildComposableStepsChildren(dbTestCase, dbSession);

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
