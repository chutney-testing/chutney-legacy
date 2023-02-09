package com.chutneytesting.tests;

import static com.chutneytesting.component.ComposableIdUtils.toInternalId;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.STEP_CLASS;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.STEP_CLASS_PROPERTY_NAME;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.component.scenario.domain.ComposableStep;
import com.chutneytesting.component.scenario.domain.ComposableStepRepository;
import com.chutneytesting.component.scenario.infra.ExecutableComposedStepMapper;
import com.chutneytesting.component.scenario.infra.ExecutableComposedTestCaseMapper;
import com.chutneytesting.component.scenario.infra.OrientComposableStepMapper;
import com.chutneytesting.component.scenario.infra.RawImplementationMapper;
import com.chutneytesting.component.scenario.infra.orient.OrientComponentDB;
import com.chutneytesting.component.scenario.infra.orient.OrientConfigurationProperties;
import com.chutneytesting.component.scenario.infra.orient.OrientDBManager;
import com.chutneytesting.component.scenario.infra.orient.changelog.OrientChangelogExecutor;
import com.chutneytesting.component.scenario.infra.wrapper.StepVertex;
import com.chutneytesting.server.core.domain.execution.Strategy;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
        implementationMapper = new RawImplementationMapper(objectMapper());
        changelogExecution = new OrientChangelogExecutor();
        stepMapper = new ExecutableComposedStepMapper(implementationMapper);
        testCaseMapper = new ExecutableComposedTestCaseMapper(stepMapper);
        orientComponentDB = initComponentDB(databaseName, dbType);
    }

    //copy from WebConfiguration
    public static ObjectMapper objectMapper() {
        return new ObjectMapper()
            //TODO ??.addMixIn(Resource.class, MyMixInForIgnoreType.class)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .findAndRegisterModules();
    }

    private OrientComponentDB initComponentDB(String databaseName, ODatabaseType dbType) {
        OrientConfigurationProperties orientConfigurationProperties = new OrientConfigurationProperties();
        orientConfigurationProperties.getDBProperties().setDbName(databaseName);
        orientConfigurationProperties.getDBProperties().setDbType(dbType.name());
        orientConfigurationProperties.setPath("./target/.chutney/orient/");
        orientConfigurationProperties.setContextConfiguration(Map.of(
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
            return dbSession.load(new ORecordId(toInternalId(recordId)));
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
                .map(oElement -> OrientComposableStepMapper.vertexToComposableStep(StepVertex.builder().from(oElement.asVertex().get()).build())).orElse(null);
        }
    }

}
