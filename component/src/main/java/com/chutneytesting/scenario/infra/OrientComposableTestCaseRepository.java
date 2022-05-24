package com.chutneytesting.scenario.infra;

import static com.chutneytesting.ComposableIdUtils.toExternalId;
import static com.chutneytesting.ComposableIdUtils.toInternalId;
import static com.chutneytesting.scenario.infra.OrientComposableTestCaseMapper.testCaseToVertex;
import static com.chutneytesting.scenario.infra.OrientComposableTestCaseMapper.vertexToTestCase;
import static com.chutneytesting.scenario.infra.orient.OrientComponentDB.TESTCASE_CLASS;
import static com.chutneytesting.scenario.infra.orient.OrientUtils.close;
import static com.chutneytesting.scenario.infra.orient.OrientUtils.deleteVertex;
import static com.chutneytesting.scenario.infra.orient.OrientUtils.load;
import static com.chutneytesting.scenario.infra.orient.OrientUtils.rollback;

import com.chutneytesting.scenario.domain.AlreadyExistingScenarioException;
import com.chutneytesting.scenario.domain.ScenarioNotFoundException;
import com.chutneytesting.scenario.domain.TestCaseMetadata;
import com.chutneytesting.scenario.domain.ComposableTestCase;
import com.chutneytesting.scenario.domain.ComposableTestCaseRepository;
import com.chutneytesting.scenario.infra.orient.OrientComponentDB;
import com.chutneytesting.scenario.infra.wrapper.TestCaseVertex;
import com.chutneytesting.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.execution.domain.ExecutableComposedTestCaseRepository;
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
public class OrientComposableTestCaseRepository implements ComposableTestCaseRepository, ExecutableComposedTestCaseRepository {

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
            rollback(dbSession);
            throw new AlreadyExistingScenarioException(e.getMessage());
        } catch (OConcurrentModificationException e) {
            rollback(dbSession);
            throw new ScenarioNotFoundException(composableTestCase.id, savedFStep.getVersion());
        } catch (ScenarioNotFoundException e) {
            rollback(dbSession);
            throw e;
        } catch (Exception e) {
            rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            close(dbSession);
        }
    }

    @Override
    public ComposableTestCase findById(String composableTestCaseId) {
        String internalId = toInternalId(composableTestCaseId);
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex) load(internalId, dbSession)
                .orElseThrow(() -> new ScenarioNotFoundException(internalId));
            return vertexToTestCase(TestCaseVertex.builder().from(element).build());
        }
    }

    @Override
    public ExecutableComposedTestCase findExecutableById(String composableTestCaseId) {
        String internalId = toInternalId(composableTestCaseId);
        ComposableTestCase composableTestCase = findById(internalId);
        return testCaseMapper.composableToExecutable(composableTestCase);
    }

    private static final String QUERY_SELECT_ALL = "SELECT @rid FROM " + TESTCASE_CLASS + "";

    @Override
    public List<TestCaseMetadata> findAll() {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allSteps = dbSession.query(QUERY_SELECT_ALL);
            return Lists.newArrayList(allSteps).stream()
                .map(rs -> {
                    OVertex element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return vertexToTestCase(TestCaseVertex.builder().from(element).build()).metadata;
                })
                .collect(Collectors.toList());
        }
    }

    @Override
    public void removeById(String testCaseId) {
        String internalId = toInternalId(testCaseId);
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            deleteVertex(internalId, dbSession);
        }
    }

    @Override
    public Integer lastVersion(String composableTestCaseId) {
        String internalId = toInternalId(composableTestCaseId);
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex) load(internalId, dbSession)
                .orElseThrow(() -> new ScenarioNotFoundException(internalId));
            return element.getVersion();
        }
    }

    @Override
    public List<TestCaseMetadata> search(String textFilter) {
        String[] words = StringEscapeUtils.escapeSql(textFilter).split("\\s");
        String fullTextSearch = Arrays.stream(words).map(w -> "+" + w + "*").collect(Collectors.joining(" "));
        String query = "SELECT @rid FROM " + TESTCASE_CLASS + " WHERE SEARCH_CLASS(\"" + fullTextSearch + "\") = true";
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allSteps = dbSession.query(query);
            return Lists.newArrayList(allSteps).stream()
                .map(rs -> {
                    OVertex element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return vertexToTestCase(TestCaseVertex.builder().from(element).build()).metadata;
                })
                .collect(Collectors.toList());
        }
    }

    private OVertex save(ComposableTestCase composableTestCase, ODatabaseSession dbSession) {
        String internalId = toInternalId(composableTestCase.id);
        Optional<OElement> stepRecord = load(internalId, dbSession);
        if (stepRecord.isPresent() && stepRecord.get().getVersion() != composableTestCase.metadata.version()) {
            throw new ScenarioNotFoundException(internalId, composableTestCase.metadata.version());
        }
        OVertex testCase = (OVertex) stepRecord.orElseGet(() -> dbSession.newVertex(TESTCASE_CLASS));
        TestCaseVertex testCaseVertex = testCaseToVertex(composableTestCase, testCase);
        return testCaseVertex.save(dbSession);
    }

}
