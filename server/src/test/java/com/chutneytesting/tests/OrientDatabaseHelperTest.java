package com.chutneytesting.tests;

import static com.chutneytesting.design.infra.storage.scenario.compose.OrientComposableStepMapper.vertexToComposableStep;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_NAME;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepRepository;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.design.infra.storage.scenario.compose.ExecutableComposedStepMapper;
import com.chutneytesting.design.infra.storage.scenario.compose.ExecutableComposedTestCaseMapper;
import com.chutneytesting.design.infra.storage.scenario.compose.RawImplementationMapper;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientConfigurationProperties;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientDBManager;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.changelog.OrientChangelogExecutor;
import com.chutneytesting.design.infra.storage.scenario.compose.wrapper.StepVertex;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.groovy.util.Maps;

public class OrientDatabaseHelperTest {

    private OrientDBManager orientDBManager;

    private final String databaseName;
    public final OrientChangelogExecutor changelogExecution;
    public final OrientComponentDB orientComponentDB;
    public final RawImplementationMapper implementationMapper;
    public final ExecutableComposedStepMapper stepMapper;
    public final ExecutableComposedTestCaseMapper testCaseMapper;

    public OrientDatabaseHelperTest(String databaseName) {
        this(databaseName, ODatabaseType.MEMORY);
    }

    public OrientDatabaseHelperTest(String databaseName, ODatabaseType dbType) {
        this.databaseName = databaseName;
        WebConfiguration webConfiguration = new WebConfiguration();
        implementationMapper = new RawImplementationMapper(webConfiguration.objectMapper());
        changelogExecution = new OrientChangelogExecutor();
        stepMapper = new ExecutableComposedStepMapper(implementationMapper);
        testCaseMapper = new ExecutableComposedTestCaseMapper(stepMapper);
        orientComponentDB = initComponentDB(databaseName, dbType);
    }

    private OrientComponentDB initComponentDB(String databaseName) {
        return initComponentDB(databaseName, ODatabaseType.MEMORY);
    }

    private OrientComponentDB initComponentDB(String databaseName, ODatabaseType dbType) {
        OrientConfigurationProperties orientConfigurationProperties = new OrientConfigurationProperties();
        orientConfigurationProperties.getDBProperties().setDbName(databaseName);
        orientConfigurationProperties.getDBProperties().setDbType(dbType.name());
        orientConfigurationProperties.setPath("./target/.chutney/orient/");
        orientConfigurationProperties.setContextConfiguration(Maps.of(
            "storage.diskCache.bufferSize", 128,
            "storage.useWAL", false
        ));

        orientDBManager = new OrientDBManager(orientConfigurationProperties);
        orientDBManager.init();
        return new OrientComponentDB(orientDBManager, orientConfigurationProperties, changelogExecution);
    }

    public void destroyDB() {
        orientDBManager.dropOrientDB(databaseName);
        orientDBManager.destroy();
    }

    public ODatabasePool dbPool() {
        return orientDBManager.getDBPool(databaseName);
    }

    public void truncateCollection(String collection) {
        try (ODatabaseSession dbSession = dbPool().acquire()) {
            dbSession.command("TRUNCATE CLASS " + collection + " UNSAFE");
        }
        assertThat(countCollectionRecords(collection)).isZero();
    }

    private long countCollectionRecords(String collection) {
        try (ODatabaseSession dbSession = dbPool().acquire()) {
            long count = dbSession.getClass(collection).count();
            dbSession.close();
            return count;
        }
    }

    public OElement loadById(String recordId) {
        try (ODatabaseSession dbSession = dbPool().acquire()) {
            return dbSession.load(new ORecordId(recordId));
        }
    }

    private static final String FIND_ELEMENTS_OF_CLASS_BY_PROPERTY = "SELECT FROM %s WHERE %s = ?";

    public Optional<OElement> loadByProperty(final String className, final String propertyName, final String propertyValue, final ODatabaseSession dbSession) {
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
                                 List<ComposableStep> subSteps) {
        return ComposableStep.builder()
            .withId(id)
            .withName(name)
            .withDefaultParameters(parameters)
            .withSteps(subSteps)
            .withImplementation(implementation)
            .withStrategy(strategy)
            .withTags(tags)
            .build();
    }

    public ComposableStep buildComposableStep(String name, Strategy strategy, ComposableStep... subSteps) {
        return build(null, name, null, null, null, strategy, asList(subSteps));
    }

    public ComposableStep buildComposableStep(String name, ComposableStep... subSteps) {
        return build(null, name, null, null, null, null, asList(subSteps));
    }

    public ComposableStep buildComposableStep(String name, Map<String, String> defaultParameters) {
        return build(null, name, defaultParameters, null, null, null, Collections.emptyList());
    }

    public ComposableStep buildComposableStep(String name, Map<String, String> parameters, ComposableStep... subSteps) {
        return build(null, name, parameters, null, null, null, asList(subSteps));
    }

    public ComposableStep buildComposableStep(String name, String implementation) {
        return build(null, name, null, null, implementation, null, null);
    }

    public ComposableStep buildComposableStep(String name, String implementation, String id) {
        return build(id, name, null, null, implementation, null, null);
    }

    public ComposableStep buildComposableStep(String name, List<String> tags) {
        return build(null, name, null, tags, null, null, null);
    }

    public ComposableStep saveAndReload(ComposableStepRepository composableStepRepository, ComposableStep composableStep) {
        composableStepRepository.save(composableStep);
        return findByName(composableStep.name);
    }

    public ComposableStep findByName(final String name) {
        try (ODatabaseSession dbSession = dbPool().acquire()) {
            return loadByProperty(STEP_CLASS, STEP_CLASS_PROPERTY_NAME, name, dbSession)
                .map(oElement -> vertexToComposableStep(StepVertex.builder().from(oElement.asVertex().get()).build())).orElse(null);
        }
    }

}
