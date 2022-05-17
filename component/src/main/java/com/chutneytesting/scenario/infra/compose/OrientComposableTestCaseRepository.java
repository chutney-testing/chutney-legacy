package com.chutneytesting.scenario.infra.compose;

import static com.chutneytesting.scenario.infra.compose.OrientComposableTestCaseMapper.testCaseToVertex;
import static com.chutneytesting.scenario.infra.compose.OrientComposableTestCaseMapper.vertexToTestCase;
import static com.chutneytesting.scenario.infra.compose.orient.OrientComponentDB.TESTCASE_CLASS;
import static com.chutneytesting.scenario.infra.compose.orient.OrientUtils.close;
import static com.chutneytesting.scenario.infra.compose.orient.OrientUtils.deleteVertex;
import static com.chutneytesting.scenario.infra.compose.orient.OrientUtils.load;
import static com.chutneytesting.scenario.infra.compose.orient.OrientUtils.rollback;

import com.chutneytesting.scenario.domain.AlreadyExistingScenarioException;
import com.chutneytesting.scenario.domain.ScenarioNotFoundException;
import com.chutneytesting.scenario.domain.TestCaseMetadata;
import com.chutneytesting.scenario.domain.compose.ComposableTestCase;
import com.chutneytesting.scenario.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.scenario.infra.compose.orient.OrientComponentDB;
import com.chutneytesting.scenario.infra.compose.wrapper.TestCaseVertex;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCaseRepository;
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
            return savedFStep.getIdentity().toString();
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
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex) load(composableTestCaseId, dbSession)
                .orElseThrow(() -> new ScenarioNotFoundException(composableTestCaseId));
            return vertexToTestCase(TestCaseVertex.builder().from(element).build());
        }
    }

    @Override
    public ExecutableComposedTestCase findExecutableById(String composableTestCaseId) {
        ComposableTestCase composableTestCase = findById(composableTestCaseId);
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
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            deleteVertex(testCaseId, dbSession);
        }
    }

    @Override
    public Integer lastVersion(String composableTestCaseId) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex) load(composableTestCaseId, dbSession)
                .orElseThrow(() -> new ScenarioNotFoundException(composableTestCaseId));
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
        Optional<OElement> stepRecord = load(composableTestCase.id, dbSession);
        if (stepRecord.isPresent() && stepRecord.get().getVersion() != composableTestCase.metadata.version()) {
            throw new ScenarioNotFoundException(composableTestCase.id, composableTestCase.metadata.version());
        }
        OVertex testCase = (OVertex) stepRecord.orElseGet(() -> dbSession.newVertex(TESTCASE_CLASS));
        TestCaseVertex testCaseVertex = testCaseToVertex(composableTestCase, testCase);
        return testCaseVertex.save(dbSession);
    }

}
