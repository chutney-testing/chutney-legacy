package com.chutneytesting.component.scenario.infra;

import static com.chutneytesting.component.ComposableIdUtils.toExternalId;
import static com.chutneytesting.component.ComposableIdUtils.toInternalId;

import com.chutneytesting.component.ComposableIdUtils;
import com.chutneytesting.component.execution.domain.ExecutableComposedStep;
import com.chutneytesting.component.execution.domain.ExecutableStepRepository;
import com.chutneytesting.component.scenario.domain.AlreadyExistingComposableStepException;
import com.chutneytesting.component.scenario.domain.ComposableStep;
import com.chutneytesting.component.scenario.domain.ComposableStepNotFoundException;
import com.chutneytesting.component.scenario.domain.ComposableStepRepository;
import com.chutneytesting.component.scenario.domain.ParentStepId;
import com.chutneytesting.component.scenario.infra.orient.OrientComponentDB;
import com.chutneytesting.component.scenario.infra.orient.OrientUtils;
import com.chutneytesting.component.scenario.infra.wrapper.StepVertex;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class OrientComposableStepRepository implements ComposableStepRepository, ExecutableStepRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientComposableStepRepository.class);

    private final ODatabasePool componentDBPool;
    private final ExecutableComposedStepMapper composedStepMapper;

    public OrientComposableStepRepository(OrientComponentDB orientComponentDB, ExecutableComposedStepMapper mapper) {
        this.componentDBPool = orientComponentDB.dbPool();
        this.composedStepMapper = mapper;
    }

    @Override
    public String save(final ComposableStep composableStep) {
        LOGGER.debug("Saving component : " + composableStep.name);
        ODatabaseSession dbSession = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            OVertex savedFStep = save(composableStep, dbSession);
            dbSession.commit();
            LOGGER.debug("Saved component : " + savedFStep.toString());
            return ComposableIdUtils.toExternalId(savedFStep.getIdentity().toString(null).toString());
        } catch (ORecordDuplicatedException e) {
            OrientUtils.rollback(dbSession);
            throw new AlreadyExistingComposableStepException(composableStep);
        } catch (Exception e) {
            OrientUtils.rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            OrientUtils.close(dbSession);
        }
    }

    @Override
    public ComposableStep findById(final String recordId) {
        String internalId = toInternalId(recordId);
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex) OrientUtils.load(internalId, dbSession)
                .orElseThrow(() -> new ComposableStepNotFoundException(internalId));
            return OrientComposableStepMapper.vertexToComposableStep(StepVertex.builder().from(element).build());
        }
    }

    @Override
    public ExecutableComposedStep findExecutableById(String recordId) {
        ComposableStep composableStep = findById(recordId);
        return composedStepMapper.composableToExecutable(composableStep);
    }

    @Override
    public void deleteById(String recordId) {
        String internalId = toInternalId(recordId);
        ODatabaseSession dbSession = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            OrientUtils.deleteVertex(internalId, dbSession);
            dbSession.commit();
            LOGGER.debug("Removed component : " + internalId);
        } catch (Exception e) {
            OrientUtils.rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            OrientUtils.close(dbSession);
        }
    }

    private static final String QUERY_SELECT_ALL = "SELECT @rid FROM " + OrientComponentDB.STEP_CLASS + "";

    @Override
    public List<ComposableStep> findAll() {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allSteps = dbSession.query(QUERY_SELECT_ALL);
            return Lists.newArrayList(allSteps).stream()
                .map(rs -> {
                    OVertex element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return OrientComposableStepMapper.vertexToComposableStep(StepVertex.builder().from(element).build());
                })
                .collect(Collectors.toList());
        }
    }

    private static final String FIND_PARENTS_STEP = "select @rid, name, title, @class from (TRAVERSE in(" + OrientComponentDB.GE_STEP_CLASS + ") FROM ?) where $depth = 1";

    @Override
    public List<ParentStepId> findParents(String stepId) {
        String internalId = toInternalId(stepId);
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            Optional<OElement> stepRecord = OrientUtils.load(internalId, dbSession);

            if (stepRecord.isPresent()) {
                List<ParentStepId> funcStepIds = new ArrayList<>();
                try (OResultSet rs = dbSession.query(FIND_PARENTS_STEP, stepRecord.get().getIdentity())) {
                    while (rs.hasNext()) {
                        OResult res = rs.next();
                        if (res.getProperty("@class").equals(OrientComponentDB.STEP_CLASS)) {
                            funcStepIds.add(new ParentStepId(toExternalId(res.getProperty("@rid").toString()), res.getProperty("name"), false));
                        } else {
                            funcStepIds.add(new ParentStepId(toExternalId(res.getProperty("@rid").toString()), res.getProperty("title"), true));
                        }
                    }
                }
                return funcStepIds;
            }
        }
        throw new ComposableStepNotFoundException(stepId);
    }

    private OVertex save(ComposableStep composableStep, final ODatabaseSession dbSession) {
        String internalId = toInternalId(composableStep.id);
        Optional<OElement> stepRecord = OrientUtils.load(internalId, dbSession);
        OVertex oVertex = (OVertex) stepRecord.orElse(dbSession.newVertex(OrientComponentDB.STEP_CLASS));

        StepVertex stepVertex = OrientComposableStepMapper.composableStepToVertex(composableStep, oVertex, dbSession);
        return stepVertex.save(dbSession);
    }
}
