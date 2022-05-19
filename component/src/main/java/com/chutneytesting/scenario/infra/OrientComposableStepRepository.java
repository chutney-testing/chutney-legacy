package com.chutneytesting.scenario.infra;

import static com.chutneytesting.scenario.infra.OrientComposableStepMapper.vertexToComposableStep;

import com.chutneytesting.scenario.domain.AlreadyExistingComposableStepException;
import com.chutneytesting.scenario.domain.ComposableStep;
import com.chutneytesting.scenario.domain.ComposableStepNotFoundException;
import com.chutneytesting.scenario.domain.ComposableStepRepository;
import com.chutneytesting.scenario.domain.ParentStepId;
import com.chutneytesting.scenario.infra.wrapper.StepVertex;
import com.chutneytesting.scenario.infra.orient.OrientComponentDB;
import com.chutneytesting.scenario.infra.orient.OrientUtils;
import com.chutneytesting.execution.domain.ExecutableComposedStep;
import com.chutneytesting.execution.domain.ExecutableStepRepository;
import com.chutneytesting.tools.ImmutablePaginatedDto;
import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestParametersDto;
import com.chutneytesting.tools.SortRequestParametersDto;
import com.chutneytesting.tools.SqlUtils;
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
import org.apache.commons.lang3.StringUtils;
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
            return savedFStep.getIdentity().toString(null).toString();
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
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex) OrientUtils.load(recordId, dbSession)
                .orElseThrow(() -> new ComposableStepNotFoundException(recordId));
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
        ODatabaseSession dbSession = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            OrientUtils.deleteVertex(recordId, dbSession);
            dbSession.commit();
            LOGGER.debug("Removed component : " + recordId);
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

    @Override
    public PaginatedDto<ComposableStep> find(PaginationRequestParametersDto paginationParameters, SortRequestParametersDto sortParameters, ComposableStep filters) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            String query = buildPaginatedQuery(filters, sortParameters);
            // Count
            long totalCount;
            try (OResultSet rs = dbSession.query(SqlUtils.count(query))) {
                totalCount = OrientUtils.resultSetToCount(rs);
            }
            // Execute
            try (OResultSet rs = dbSession.query(OrientUtils.addPaginationParameters(query), paginationParameters.start() - 1, paginationParameters.limit())) {
                List<ComposableStep> fSteps = Lists.newArrayList(rs)
                    .stream()
                    .filter(e -> e.getVertex().isPresent())
                    .map(element -> OrientComposableStepMapper.vertexToComposableStep(StepVertex.builder().from(element.getVertex().get()).build()))
                    .collect(Collectors.toList());
                return ImmutablePaginatedDto.<ComposableStep>builder()
                    .totalCount(totalCount)
                    .addAllData(fSteps)
                    .build();
            }
        }
    }

    private static final String FIND_PARENTS_STEP = "select @rid, name, title, @class from (TRAVERSE in(" + OrientComponentDB.GE_STEP_CLASS + ") FROM ?) where $depth = 1";

    @Override
    public List<ParentStepId> findParents(String stepId) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            Optional<OElement> stepRecord = OrientUtils.load(stepId, dbSession);

            if (stepRecord.isPresent()) {
                List<ParentStepId> funcStepIds = new ArrayList<>();
                try (OResultSet rs = dbSession.query(FIND_PARENTS_STEP, stepRecord.get().getIdentity())) {
                    while (rs.hasNext()) {
                        OResult res = rs.next();
                        if (res.getProperty("@class").equals(OrientComponentDB.STEP_CLASS)) {
                            funcStepIds.add(new ParentStepId(res.getProperty("@rid").toString(), res.getProperty("name"), false));
                        } else {
                            funcStepIds.add(new ParentStepId(res.getProperty("@rid").toString(), res.getProperty("title"), true));
                        }
                    }
                }
                return funcStepIds;
            }
        }
        throw new ComposableStepNotFoundException(stepId);
    }

    private OVertex save(ComposableStep composableStep, final ODatabaseSession dbSession) {
        Optional<OElement> stepRecord = OrientUtils.load(composableStep.id, dbSession);
        OVertex oVertex = (OVertex) stepRecord.orElse(dbSession.newVertex(OrientComponentDB.STEP_CLASS));

        StepVertex stepVertex = OrientComposableStepMapper.composableStepToVertex(composableStep, oVertex, dbSession);
        return stepVertex.save(dbSession);
    }

    private String buildPaginatedQuery(ComposableStep findParameters, SortRequestParametersDto sortParameters) {
        StringBuilder query = new StringBuilder("SELECT FROM ")
            .append(OrientComponentDB.STEP_CLASS)
            .append(" WHERE 1=1");

        // Name filter
        if (StringUtils.isNotEmpty(findParameters.name)) {
            query.append(" AND name CONTAINSTEXT '").append(findParameters.name).append("'");
        }

        // Sort
        List<String> sortAttributes = sortParameters.sortParameters();
        List<String> descAttributes = sortParameters.descParameters();
        sortAttributes.forEach(sortAttribute -> {
            query.append(" ORDER BY ").append(sortAttribute);
            if (descAttributes.contains(sortAttribute)) {
                query.append(" DESC");
            }
        });

        return query.toString();
    }
}
