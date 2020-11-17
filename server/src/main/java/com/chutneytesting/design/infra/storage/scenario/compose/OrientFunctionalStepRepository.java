package com.chutneytesting.design.infra.storage.scenario.compose;

import static com.chutneytesting.design.infra.storage.scenario.compose.OrientFunctionalStepMapper.functionalStepToVertex;
import static com.chutneytesting.design.infra.storage.scenario.compose.OrientFunctionalStepMapper.vertexToFunctionalStep;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_NAME;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_USAGE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.close;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.deleteVertex;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.load;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.rollback;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.lucene.LuceneUtils.cleanFirstWildcardsCharacters;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.lucene.LuceneUtils.escapeLuceneSearchQuery;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.lucene.LuceneUtils.forceAllRequiredTerm;

import com.chutneytesting.design.domain.compose.AlreadyExistingFunctionalStepException;
import com.chutneytesting.design.domain.scenario.compose.FunctionalStep;
import com.chutneytesting.design.domain.scenario.compose.FunctionalStepCyclicDependencyException;
import com.chutneytesting.design.domain.scenario.compose.FunctionalStepNotFoundException;
import com.chutneytesting.design.domain.scenario.compose.ParentStepId;
import com.chutneytesting.design.domain.scenario.compose.StepRepository;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class OrientFunctionalStepRepository implements StepRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientFunctionalStepRepository.class);

    private ODatabasePool componentDBPool;

    public OrientFunctionalStepRepository(OrientComponentDB orientComponentDB) {
        this.componentDBPool = orientComponentDB.dbPool();
    }

    @Override
    public String save(final FunctionalStep functionalStep) {
        LOGGER.debug("Step save " + functionalStep.name);
        ODatabaseSession dbSession = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            OVertex savedFStep = save(functionalStep, dbSession);
            checkFunctionalStepCyclicDependency(savedFStep);
            dbSession.commit();
            LOGGER.debug("Save step : " + savedFStep.toString());
            return savedFStep.getIdentity().toString(null).toString();
        } catch (ORecordDuplicatedException e) {
            rollback(dbSession);
            throw new AlreadyExistingFunctionalStepException(e.getMessage());
        } catch (FunctionalStepCyclicDependencyException e) {
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
    public FunctionalStep findById(final String recordId) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OVertex element = (OVertex)load(recordId, dbSession)
                .orElseThrow(FunctionalStepNotFoundException::new);
            return vertexToFunctionalStep(element, dbSession).build();
        }
    }

    @Override
    public void deleteById(String recordId) {
        ODatabaseSession dbSession = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            deleteVertex(recordId, dbSession);
            dbSession.commit();
            LOGGER.debug("Delete step : " + recordId);
        } catch (Exception e) {
            rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            close(dbSession);
        }
    }

    private static final String QUERY_SELECT_ALL = "SELECT @rid FROM " + STEP_CLASS + "";

    @Override
    public List<FunctionalStep> findAll() {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allSteps = dbSession.query(QUERY_SELECT_ALL);
            return Lists.newArrayList(allSteps).stream()
                .map(rs -> {
                    OVertex element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return vertexToFunctionalStep(element, dbSession).build();
                })
                .collect(Collectors.toList());
        }
    }

    private static final String QUERY_FSTEPS_NAME_LUCENE_INDEX = "SELECT @rid, name FROM " + STEP_CLASS + " WHERE SEARCH_CLASS(?) = true LIMIT 10";

    @Override
    public List<FunctionalStep> queryByName(String searchQuery) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            try (OResultSet rs = dbSession.query(QUERY_FSTEPS_NAME_LUCENE_INDEX, forceAllRequiredTerm(escapeLuceneSearchQuery(searchQuery)))) {
                return Lists.newArrayList(rs)
                    .stream()
                    .map(result ->
                        FunctionalStep.builder()
                            .withId(result.getProperty("@rid").toString())
                            .withName(result.getProperty("name"))
                            .build()
                    )
                    .collect(Collectors.toList());
            }
        }
    }

    @Override
    public PaginatedDto<FunctionalStep> find(PaginationRequestParametersDto paginationParameters, SortRequestParametersDto sortParameters, FunctionalStep filters) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            String query = buildPaginatedQuery(filters, sortParameters);
            // Count
            long totalCount;
            try (OResultSet rs = dbSession.query(SqlUtils.count(query))) {
                totalCount = OrientUtils.resultSetToCount(rs);
            }
            // Execute
            try (OResultSet rs = dbSession.query(OrientUtils.addPaginationParameters(query), paginationParameters.start() - 1, paginationParameters.limit())) {
                List<FunctionalStep> fSteps = Lists.newArrayList(rs)
                    .stream()
                    .filter(e -> e.getVertex().isPresent())
                    .map(element -> vertexToFunctionalStep(element.getVertex().get(), dbSession).build())
                    .collect(Collectors.toList());
                return ImmutablePaginatedDto.<FunctionalStep>builder()
                    .totalCount(totalCount)
                    .addAllData(fSteps)
                    .build();
            }
        }
    }

    private static final String FIND_PARENTS_STEP = "select @rid, name, title, @class from (TRAVERSE in("+ GE_STEP_CLASS +") FROM ?) where $depth = 1";

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
        throw new FunctionalStepNotFoundException();
    }

    private OVertex save(FunctionalStep functionalStep, final ODatabaseSession dbSession) {
        Optional<OElement> stepRecord = load(functionalStep.id, dbSession);
        OVertex step = (OVertex) stepRecord.orElse(dbSession.newVertex(STEP_CLASS));
        functionalStepToVertex(functionalStep, step, dbSession);
        updateParentsDataSets(functionalStep, step);
        return step.save();
    }

    private void updateParentsDataSets(FunctionalStep functionalStep, OVertex step) {
        step.getEdges(ODirection.IN, GE_STEP_CLASS)
            .forEach(parentEdge -> {
                Map<String, String> dataSet = parentEdge.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS);
                if (dataSet != null) {
                    Map<String, String> newDataSet = new HashMap<>();
                    functionalStep.parameters.forEach((paramKey, paramValue) ->
                        newDataSet.put(paramKey, dataSet.getOrDefault(paramKey, paramValue))
                    );
                    parentEdge.setProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS, newDataSet);
                    parentEdge.save();
                }
            });
    }

    private void checkFunctionalStepCyclicDependency(OVertex savedFStep) {
        checkCyclicDependency(savedFStep, new ArrayList<>());
    }

    private void checkCyclicDependency(final OVertex savedFStep, List<ORecordId> parentsIds) {
        parentsIds.add((ORecordId) savedFStep.getIdentity());
        List<OVertex> children = new ArrayList<>();
        savedFStep.getEdges(ODirection.OUT, GE_STEP_CLASS).forEach(oEdge -> children.add(oEdge.getTo()));
        if (children.size() > 0) {
            List<ORecordId> childrenIds = new ArrayList<>();
            for (OElement child : children) {
                childrenIds.add((ORecordId) child.getIdentity());
            }
            if (childrenIds.removeAll(parentsIds)) {
                throw new FunctionalStepCyclicDependencyException("Cyclic dependency found on functional step [" + savedFStep.getProperty(STEP_CLASS_PROPERTY_NAME) + "]");
            }
            children.forEach(oElement -> checkCyclicDependency(oElement, new ArrayList<>(parentsIds)));
        }
    }

    private String buildPaginatedQuery(FunctionalStep findParameters, SortRequestParametersDto sortParameters) {
        StringBuilder query = new StringBuilder("SELECT FROM ")
            .append(STEP_CLASS)
            .append(" WHERE 1=1");

        // Name filter
        if (StringUtils.isNotEmpty(findParameters.name)) {
            query.append(" AND SEARCH_CLASS('").append(cleanFirstWildcardsCharacters(findParameters.name)).append("')=true");
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
