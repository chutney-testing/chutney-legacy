package com.chutneytesting.tests;

import static com.chutneytesting.design.infra.storage.scenario.compose.OrientComposableStepMapper.vertexToComposableStep;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepRepository;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.design.infra.storage.scenario.compose.ExecutableComposedStepMapper;
import com.chutneytesting.design.infra.storage.scenario.compose.ExecutableComposedTestCaseMapper;
import com.chutneytesting.design.infra.storage.scenario.compose.RawImplementationMapper;
import com.chutneytesting.design.infra.storage.scenario.compose.wrapper.StepVertex;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientConfigurationProperties;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientDBManager;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.changelog.OrientChangelogExecutor;
import com.chutneytesting.task.api.EmbeddedTaskEngine;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.groovy.util.Maps;
import org.mockito.Mockito;

public abstract class AbstractOrientDatabaseTest {

    private static OrientDBManager orientDBManager;
    private static final WebConfiguration webConfiguration = new WebConfiguration();

    protected static final String DATABASE_NAME = "orient_repo_test";
    protected static OrientComponentDB orientComponentDB;
    protected static OrientChangelogExecutor changelogExecution;

    protected static EmbeddedTaskEngine mockEngine = Mockito.mock(EmbeddedTaskEngine.class);
    protected static RawImplementationMapper implementationMapper = new RawImplementationMapper(webConfiguration.objectMapper());
    protected static ExecutableComposedStepMapper stepMapper = new ExecutableComposedStepMapper(implementationMapper);
    protected static ExecutableComposedTestCaseMapper testCaseMapper = new ExecutableComposedTestCaseMapper(stepMapper);

    protected static void initComponentDB(String databaseName) {
        initComponentDB(databaseName, ODatabaseType.MEMORY);
    }

    protected static void initComponentDB(String databaseName, ODatabaseType dbType) {
        OrientConfigurationProperties orientConfigurationProperties = new OrientConfigurationProperties();
        orientConfigurationProperties.getDBProperties().setDbName(databaseName);
        orientConfigurationProperties.getDBProperties().setDbType(dbType.name());
        orientConfigurationProperties.setPath("./target/.chutney/orient/");
        orientConfigurationProperties.setContextConfiguration(Maps.of(
            "storage.diskCache.bufferSize", 128,
            "storage.useWAL", false
        ));

        changelogExecution = new OrientChangelogExecutor();
        orientDBManager = new OrientDBManager(orientConfigurationProperties);
        orientDBManager.init();
        orientComponentDB = new OrientComponentDB(orientDBManager, orientConfigurationProperties, changelogExecution);
    }

    protected static void destroyDB(String databaseName) {
        orientDBManager.dropOrientDB(databaseName);
        orientDBManager.destroy();
    }

    protected ODatabasePool dbPool(String databaseName) {
        return orientDBManager.getDBPool(databaseName);
    }

    protected void truncateCollection(String databaseName, String collection) {
        try (ODatabaseSession dbSession = dbPool(databaseName).acquire()) {
            dbSession.command("TRUNCATE CLASS " + collection + " UNSAFE");
        }
        assertThat(countCollectionRecords(databaseName, collection)).isZero();
    }

    private long countCollectionRecords(String databaseName, String collection) {
        try (ODatabaseSession dbSession = dbPool(databaseName).acquire()) {
            long count = dbSession.getClass(collection).count();
            dbSession.close();
            return count;
        }
    }

    protected OElement loadById(String recordId) {
        try (ODatabaseSession dbSession = dbPool(AbstractOrientDatabaseTest.DATABASE_NAME).acquire()) {
            return dbSession.load(new ORecordId(recordId));
        }
    }

    private static final String FIND_ELEMENTS_OF_CLASS_BY_PROPERTY = "SELECT FROM %s WHERE %s = ?";

    protected Optional<OElement> loadByProperty(final String className, final String propertyName, final String propertyValue, final ODatabaseSession dbSession) {
        try (OResultSet rs = dbSession.query(String.format(FIND_ELEMENTS_OF_CLASS_BY_PROPERTY, className, propertyName), propertyValue)) {
            if (rs.hasNext()) {
                return rs.next().getElement();
            }
        }
        return Optional.empty();
    }

    private ComposableStep build(String id,
                                 String name,
                                 Map<String, String> parameters,
                                 List<String> tags,
                                 String implementation,
                                 Strategy strategy,
                                 ComposableStep... subSteps) {
        ComposableStep.ComposableStepBuilder builder = ComposableStep.builder();
        if (id != null) {
            builder.withId(id);
        }
        if (name != null) {
            builder.withName(name);
        }
        if (parameters != null) {
            builder.withDefaultParameters(parameters);
        }
        if (subSteps != null) {
            builder.withSteps(Arrays.asList(subSteps));
        }
        if (implementation != null) {
            builder.withImplementation(Optional.of(implementation));
        }
        if (strategy != null) {
            builder.withStrategy(strategy);
        }
        if (tags != null) {
            builder.withTags(tags);
        }
        return builder.build();
    }

    protected ComposableStep buildComposableStep(String name, Strategy strategy, ComposableStep... subSteps) {
        return build(null, name, null, null, null, strategy, subSteps);
    }

    protected ComposableStep buildComposableStep(String name, ComposableStep... subSteps) {
        return build(null, name, null, null, null, null, subSteps);
    }

    protected ComposableStep buildComposableStep(String name, Map<String, String> parameters) {
        return  build(null, name, parameters, null, null, null);
    }

    protected ComposableStep buildComposableStep(String name, Map<String, String> parameters, ComposableStep... subSteps) {
        return  build(null, name, parameters, null, null, null, subSteps);
    }

    protected ComposableStep buildComposableStep(String name, String implementation) {
        return build(null, name, null, null, implementation, null, null);
    }

    protected ComposableStep buildComposableStep(String name, String implementation, String id) {
        return build(id, name, null, null, implementation, null, null);
    }

    protected ComposableStep buildComposableStep(String name, List<String> tags) {
        return build(null, name, null, tags, null, null, null);
    }

    protected ComposableStep saveAndReload(ComposableStepRepository funcComposableStepRepository, ComposableStep composableStep) {
        funcComposableStepRepository.save(composableStep);
        return findByName(composableStep.name);
    }

    protected ComposableStep findByName(final String name) {
        try (ODatabaseSession dbSession = dbPool(DATABASE_NAME).acquire()) {
            return loadByProperty(STEP_CLASS, STEP_CLASS_PROPERTY_NAME, name, dbSession)
                .map(oElement -> vertexToComposableStep(StepVertex.builder().from(oElement.asVertex().get()).build()).build()).orElse(null);
        }
    }

}
