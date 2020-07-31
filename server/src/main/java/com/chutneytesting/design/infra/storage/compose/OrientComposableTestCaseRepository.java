package com.chutneytesting.design.infra.storage.compose;

import static com.chutneytesting.design.infra.storage.compose.OrientComposableTestCaseMapper.testCaseToVertex;
import static com.chutneytesting.design.infra.storage.compose.OrientComposableTestCaseMapper.vertexToTestCase;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.TESTCASE_CLASS;
import static com.chutneytesting.design.infra.storage.db.orient.OrientUtils.close;
import static com.chutneytesting.design.infra.storage.db.orient.OrientUtils.deleteVertex;
import static com.chutneytesting.design.infra.storage.db.orient.OrientUtils.load;
import static com.chutneytesting.design.infra.storage.db.orient.OrientUtils.rollback;

import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class OrientComposableTestCaseRepository implements ComposableTestCaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientComposableTestCaseRepository.class);

    private ODatabasePool componentDBPool;

    public OrientComposableTestCaseRepository(OrientComponentDB orientComponentDB) {
        this.componentDBPool = orientComponentDB.dbPool();
    }

    @Override
    public String save(ComposableTestCase composableTestCase) {
        ODatabaseSession dbSession = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            OVertex savedFStep = save(composableTestCase, dbSession);
            dbSession.commit();
            LOGGER.debug("Save scenario :" + savedFStep.toString());
            return savedFStep.getIdentity().toString(null).toString();
        } catch (ORecordDuplicatedException e) {
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
            OVertex element = (OVertex)load(composableTestCaseId, dbSession)
                .orElseThrow(() -> new ScenarioNotFoundException(composableTestCaseId));
            return vertexToTestCase(element, dbSession);
        }
    }

    private static final String QUERY_SELECT_ALL = "SELECT @rid FROM " + TESTCASE_CLASS + "";

    @Override
    public List<TestCaseMetadata> findAll() {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allSteps = dbSession.query(QUERY_SELECT_ALL);
            return Lists.newArrayList(allSteps).stream()
                .map(rs ->  {
                    OVertex element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return vertexToTestCase(element, dbSession).metadata;
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

    private OVertex save(ComposableTestCase composableTestCase, ODatabaseSession dbSession) {
        Optional<OElement> stepRecord = load(composableTestCase.id, dbSession);
        OVertex testCase = (OVertex) stepRecord.orElseGet(() -> dbSession.newVertex(TESTCASE_CLASS));
        testCaseToVertex(composableTestCase, testCase, dbSession);
        return testCase.save();
    }
}
