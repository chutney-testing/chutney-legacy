package com.chutneytesting.component.scenario.infra;

import static com.chutneytesting.component.ComposableIdUtils.toExternalId;
import static com.chutneytesting.component.ComposableIdUtils.toInternalId;
import static java.util.Optional.of;

import com.chutneytesting.component.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.component.execution.domain.ExecutableComposedTestCaseRepository;
import com.chutneytesting.component.scenario.domain.ComposableTestCase;
import com.chutneytesting.component.scenario.infra.orient.OrientComponentDB;
import com.chutneytesting.component.scenario.infra.orient.OrientUtils;
import com.chutneytesting.component.scenario.infra.wrapper.TestCaseVertex;
import com.chutneytesting.server.core.scenario.AggregatedRepository;
import com.chutneytesting.server.core.scenario.AlreadyExistingScenarioException;
import com.chutneytesting.server.core.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.scenario.TestCaseMetadata;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class OrientComposableTestCaseRepository implements AggregatedRepository<ComposableTestCase>, ExecutableComposedTestCaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientComposableTestCaseRepository.class);

    private final ODatabasePool componentDBPool;
    private final ExecutableComposedTestCaseMapper testCaseMapper;

    public OrientComposableTestCaseRepository(OrientComponentDB orientComponentDB, ExecutableComposedTestCaseMapper testCaseMapper) {
        this.componentDBPool = orientComponentDB.dbPool();
        this.testCaseMapper = testCaseMapper;
    }

    @Override
    public String save(ComposableTestCase composableTestCase) {
        ODatabaseSession dbSession = null;
        OVertex savedFStep = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            savedFStep = save(composableTestCase, dbSession);
            dbSession.commit();
            LOGGER.debug("Save scenario :" + savedFStep.toString());
            return toExternalId(savedFStep.getIdentity().toString());
        } catch (ORecordDuplicatedException e) {
            OrientUtils.rollback(dbSession);
            throw new AlreadyExistingScenarioException(e.getMessage());
        } catch (OConcurrentModificationException e) {
            OrientUtils.rollback(dbSession);
            throw new ScenarioNotFoundException(composableTestCase.id, savedFStep.getVersion());
        } catch (ScenarioNotFoundException e) {
            OrientUtils.rollback(dbSession);
            throw e;
        } catch (Exception e) {
            OrientUtils.rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            OrientUtils.close(dbSession);
        }
    }

    @Override
    public Optional<ComposableTestCase> findById(String composableTestCaseId) {
        String internalId = toInternalId(composableTestCaseId);
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex) OrientUtils.load(internalId, dbSession)
                .orElseThrow(() -> new ScenarioNotFoundException(internalId));

            return Optional.ofNullable(OrientComposableTestCaseMapper.vertexToTestCase(TestCaseVertex.builder().from(element).build()));
        }
    }

    @Override
    //TODO
    public Optional<TestCaseMetadata> findMetadataById(String testCaseId) {
        return Optional.empty();
    }

    @Override
    public ExecutableComposedTestCase findExecutableById(String composableTestCaseId) {
        String internalId = toInternalId(composableTestCaseId);
        Optional<ComposableTestCase> composableTestCase = findById(internalId);
        if(composableTestCase.isPresent()) {
            return testCaseMapper.composableToExecutable((ComposableTestCase) composableTestCase.get());
        } else {
            throw new ScenarioNotFoundException(composableTestCaseId);
        }

    }

    private static final String QUERY_SELECT_ALL = "SELECT @rid FROM " + OrientComponentDB.TESTCASE_CLASS + "";

    @Override
    public List<TestCaseMetadata> findAll() {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allSteps = dbSession.query(QUERY_SELECT_ALL);
            return Lists.newArrayList(allSteps).stream()
                .map(rs -> {
                    OVertex element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return OrientComposableTestCaseMapper.vertexToTestCase(TestCaseVertex.builder().from(element).build()).metadata;
                })
                .collect(Collectors.toList());
        }
    }

    @Override
    public void removeById(String testCaseId) {
        String internalId = toInternalId(testCaseId);
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OrientUtils.deleteVertex(internalId, dbSession);
        }
    }

    @Override
    public Optional<Integer> lastVersion(String composableTestCaseId) {
        String internalId = toInternalId(composableTestCaseId);
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex) OrientUtils.load(internalId, dbSession)
                .orElseThrow(() -> new ScenarioNotFoundException(internalId));
            return of(element.getVersion());
        }
    }

    @Override
    public List<TestCaseMetadata> search(String textFilter) {
        String[] words = StringEscapeUtils.escapeSql(textFilter).split("\\s");
        String fullTextSearch = Arrays.stream(words).map(w -> "+" + w + "*").collect(Collectors.joining(" "));
        String query = "SELECT @rid FROM " + OrientComponentDB.TESTCASE_CLASS + " WHERE SEARCH_CLASS(\"" + fullTextSearch + "\") = true";
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allSteps = dbSession.query(query);
            return Lists.newArrayList(allSteps).stream()
                .map(rs -> {
                    OVertex element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return OrientComposableTestCaseMapper.vertexToTestCase(TestCaseVertex.builder().from(element).build()).metadata;
                })
                .collect(Collectors.toList());
        }
    }

    private OVertex save(ComposableTestCase composableTestCase, ODatabaseSession dbSession) {
        String internalId = toInternalId(composableTestCase.id);
        Optional<OElement> stepRecord = OrientUtils.load(internalId, dbSession);
        if (stepRecord.isPresent() && stepRecord.get().getVersion() != composableTestCase.metadata.version()) {
            throw new ScenarioNotFoundException(internalId, composableTestCase.metadata.version());
        }
        OVertex testCase = (OVertex) stepRecord.orElseGet(() -> dbSession.newVertex(OrientComponentDB.TESTCASE_CLASS));
        TestCaseVertex testCaseVertex = OrientComposableTestCaseMapper.testCaseToVertex(composableTestCase, testCase);
        return testCaseVertex.save(dbSession);
    }

}
