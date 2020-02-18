package com.chutneytesting.design.infra.storage.compose;

import static com.chutneytesting.design.domain.compose.ComposableTestCaseRepository.COMPOSABLE_TESTCASE_REPOSITORY_SOURCE;
import static com.chutneytesting.design.infra.storage.compose.OrientFunctionalStepMapper.buildFunctionalStepsChildren;
import static com.chutneytesting.design.infra.storage.compose.OrientFunctionalStepMapper.setFunctionalStepVertexDenotations;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_DESCRIPTION;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TITLE;
import static com.chutneytesting.design.infra.storage.db.orient.OrientUtils.setOnlyOnceProperty;
import static com.chutneytesting.design.infra.storage.db.orient.OrientUtils.setOrRemoveProperty;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.util.Date;
import java.util.List;
import java.util.Map;

class OrientComposableTestCaseMapper {

    static void testCaseToVertex(final ComposableTestCase composableTestCase, OVertex dbTestCase, ODatabaseSession dbSession) {
        dbTestCase.setProperty(TESTCASE_CLASS_PROPERTY_TITLE, composableTestCase.metadata.title(), OType.STRING);
        setOrRemoveProperty(dbTestCase, TESTCASE_CLASS_PROPERTY_DESCRIPTION, composableTestCase.metadata.description(), OType.STRING);
        setOnlyOnceProperty(dbTestCase, TESTCASE_CLASS_PROPERTY_CREATIONDATE, Date.from(composableTestCase.metadata.creationDate()), OType.DATETIME);
        dbTestCase.setProperty(TESTCASE_CLASS_PROPERTY_TAGS, composableTestCase.metadata.tags(), OType.EMBEDDEDLIST);
        setOrRemoveProperty(dbTestCase, TESTCASE_CLASS_PROPERTY_PARAMETERS, composableTestCase.composableScenario.parameters, OType.EMBEDDEDMAP);
        setFunctionalStepVertexDenotations(dbTestCase, composableTestCase.composableScenario.functionalSteps, dbSession);
    }

    static ComposableTestCase vertexToTestCase(final OVertex dbTestCase, ODatabaseSession dbSession) {
        TestCaseMetadata metadata = TestCaseMetadataImpl.builder()
            .withId(dbTestCase.getIdentity().toString())
            .withTitle(dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_TITLE))
            .withDescription(dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_DESCRIPTION))
            .withCreationDate(((Date)dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_CREATIONDATE)).toInstant())
            .withRepositorySource(COMPOSABLE_TESTCASE_REPOSITORY_SOURCE)
            .withTags(dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_TAGS))
            .build();

        List<FunctionalStep> functionalStepRefs = buildFunctionalStepsChildren(dbTestCase, dbSession);

        Map<String, String> parameters = dbTestCase.getProperty(TESTCASE_CLASS_PROPERTY_PARAMETERS);

        return new ComposableTestCase(
            dbTestCase.getIdentity().toString(),
            metadata,
            ComposableScenario.builder()
                .withFunctionalSteps(functionalStepRefs)
                .withParameters(parameters)
                .build());
    }
}
