package com.chutneytesting.design.infra.storage.scenario.compose;

import static com.chutneytesting.design.infra.storage.scenario.compose.OrientComposableStepMapper.composableStepToVertex;
import static com.chutneytesting.design.infra.storage.scenario.compose.OrientComposableStepMapper.vertexToComposableStep;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_NAME;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_USAGE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.close;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.deleteVertex;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.load;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.rollback;

import com.chutneytesting.design.domain.scenario.compose.AlreadyExistingComposableStepException;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepCyclicDependencyException;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepNotFoundException;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepRepository;
import com.chutneytesting.design.domain.scenario.compose.ParentStepId;
import com.chutneytesting.design.infra.storage.scenario.compose.dto.StepVertex;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableStepRepository;
import com.chutneytesting.tools.ImmutablePaginatedDto;
import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestParametersDto;
import com.chutneytesting.tools.SortRequestParametersDto;
import com.chutneytesting.tools.SqlUtils;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import java.util.ArrayList;
import java.util.Collections;
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
            rollback(dbSession);
            throw new AlreadyExistingComposableStepException(composableStep);
        } catch (Exception e) {
            rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            close(dbSession);
        }
    }

    @Override
    public ComposableStep findById(final String recordId) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex) load(recordId, dbSession)
                .orElseThrow(() -> new ComposableStepNotFoundException(recordId));
            return vertexToComposableStep(element).build();
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
            deleteVertex(recordId, dbSession);
            dbSession.commit();
            LOGGER.debug("Removed component : " + recordId);
        } catch (Exception e) {
            rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            close(dbSession);
        }
    }

    private static final String QUERY_SELECT_ALL = "SELECT @rid FROM " + STEP_CLASS + "";

    @Override
    public List<ComposableStep> findAll() {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allSteps = dbSession.query(QUERY_SELECT_ALL);
            return Lists.newArrayList(allSteps).stream()
                .map(rs -> {
                    OVertex element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return vertexToComposableStep(element).build();
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
                    .map(element -> vertexToComposableStep(element.getVertex().get()).build())
                    .collect(Collectors.toList());
                return ImmutablePaginatedDto.<ComposableStep>builder()
                    .totalCount(totalCount)
                    .addAllData(fSteps)
                    .build();
            }
        }
    }

    private static final String FIND_PARENTS_STEP = "select @rid, name, title, @class from (TRAVERSE in(" + GE_STEP_CLASS + ") FROM ?) where $depth = 1";

    @Override
    public List<ParentStepId> findParents(String stepId) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            Optional<OElement> stepRecord = load(stepId, dbSession);

            if (stepRecord.isPresent()) {
                List<ParentStepId> funcStepIds = new ArrayList<>();
                try (OResultSet rs = dbSession.query(FIND_PARENTS_STEP, stepRecord.get().getIdentity())) {
                    while (rs.hasNext()) {
                        OResult res = rs.next();
                        if (res.getProperty("@class").equals(STEP_CLASS)) {
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
        Optional<OElement> stepRecord = load(composableStep.id, dbSession);
        OVertex oVertex = (OVertex) stepRecord.orElse(dbSession.newVertex(STEP_CLASS));

        StepVertex stepVertex = composableStepToVertex(composableStep, oVertex, dbSession);
        return stepVertex.save();
    }

    private String buildPaginatedQuery(ComposableStep findParameters, SortRequestParametersDto sortParameters) {
        StringBuilder query = new StringBuilder("SELECT FROM ")
            .append(STEP_CLASS)
            .append(" WHERE 1=1");

        // Name filter
        if (StringUtils.isNotEmpty(findParameters.name)) {
            query.append(" AND name CONTAINSTEXT '").append(findParameters.name).append("'");
        }

        // Usage filter
        findParameters.usage.ifPresent(stepUsage -> query.append(" AND ").append(STEP_CLASS_PROPERTY_USAGE).append("='").append(stepUsage.name()).append("'"));

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
