package com.chutneytesting.design.infra.storage.compose;

import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.GE_STEP_CLASS;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_RANK;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.STEP_CLASS_PROPERTY_IMPLEMENTATION;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.STEP_CLASS_PROPERTY_NAME;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.STEP_CLASS_PROPERTY_STRATEGY;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.STEP_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.STEP_CLASS_PROPERTY_USAGE;
import static com.chutneytesting.design.infra.storage.db.orient.OrientUtils.load;
import static com.chutneytesting.design.infra.storage.db.orient.OrientUtils.reloadIfDirty;
import static com.chutneytesting.design.infra.storage.db.orient.OrientUtils.setOrRemoveProperty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.StepUsage;
import com.chutneytesting.design.domain.compose.Strategy;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrientFunctionalStepMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientFunctionalStepMapper.class);

    static void functionalStepToVertex(final FunctionalStep functionalStep, OVertex step, ODatabaseSession dbSession) {
        step.setProperty(STEP_CLASS_PROPERTY_NAME, functionalStep.name, OType.STRING);
        setOrRemoveProperty(step, STEP_CLASS_PROPERTY_USAGE, functionalStep.usage, OType.STRING);
        setOrRemoveProperty(step, STEP_CLASS_PROPERTY_IMPLEMENTATION, functionalStep.implementation, OType.STRING);
        setOrRemoveProperty(step, STEP_CLASS_PROPERTY_TAGS, functionalStep.tags, OType.EMBEDDEDLIST);

        OElement strategy = dbSession.newElement();
        strategy.setProperty("name", functionalStep.strategy.type, OType.STRING);
        strategy.setProperty("parameters", functionalStep.strategy.parameters, OType.EMBEDDEDMAP);

        setOrRemoveProperty(step, STEP_CLASS_PROPERTY_STRATEGY, strategy, OType.EMBEDDED);
        step.setProperty(STEP_CLASS_PROPERTY_PARAMETERS, functionalStep.parameters, OType.EMBEDDEDMAP);
        setFunctionalStepVertexDenotations(step, functionalStep.steps, dbSession);
    }

    static void setFunctionalStepVertexDenotations(OVertex vertex, List<FunctionalStep> edgesToSave, ODatabaseSession dbSession) {
        vertex.getEdges(ODirection.OUT, GE_STEP_CLASS).forEach(ORecord::delete);
        IntStream.range(0, edgesToSave.size())
            .forEach(index -> {
                final FunctionalStep subFunctionalStepRef = edgesToSave.get(index);
                OVertex dbSubFunctionalStep = (OVertex) load(subFunctionalStepRef.id, dbSession)
                    .orElseThrow(() -> new IllegalArgumentException("Functional step with id [" + subFunctionalStepRef.id + "] does not exists"));
                final Map<String, String> subFunctionalStepDataSet = vertexToDataSet(dbSubFunctionalStep, dbSession);
                Map<String, String> parameters = cleanChildOverloadedParametersMap(subFunctionalStepRef.dataSet, subFunctionalStepDataSet);

                OEdge childEdge = vertex.addEdge(dbSubFunctionalStep, GE_STEP_CLASS);
                childEdge.setProperty(GE_STEP_CLASS_PROPERTY_RANK, index);
                if (!parameters.isEmpty()) {
                    childEdge.setProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS, parameters, OType.EMBEDDEDMAP);
                }
                childEdge.save();
            });
    }

    private static Map<String, String> cleanChildOverloadedParametersMap(final Map<String, String> instanceDataSet, final Map<String, String> dbDataSet) {
        return instanceDataSet.entrySet().stream()
            .filter(entry -> dbDataSet.containsKey(entry.getKey()))
            .filter(entry -> !dbDataSet.get(entry.getKey()).equals(entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static FunctionalStep.FunctionalStepBuilder vertexToFunctionalStep(final OVertex vertex, final ODatabaseSession dbSession) {
        reloadIfDirty(vertex);

        FunctionalStep.FunctionalStepBuilder builder = FunctionalStep.builder()
            .withId(vertex.getIdentity().toString())
            .withName(vertex.getProperty(STEP_CLASS_PROPERTY_NAME))
            .withTags(vertex.getProperty(STEP_CLASS_PROPERTY_TAGS));

        String usage = vertex.getProperty(STEP_CLASS_PROPERTY_USAGE);
        ofNullable(usage).ifPresent(u -> builder.withUsage(Optional.of(StepUsage.valueOf(u))));

        builder.withImplementation(ofNullable(vertex.getProperty(STEP_CLASS_PROPERTY_IMPLEMENTATION)));

        Map<String, String> parameters = vertex.getProperty(STEP_CLASS_PROPERTY_PARAMETERS);
        ofNullable(parameters).ifPresent(builder::addParameters);

        OElement strategy = vertex.getProperty(STEP_CLASS_PROPERTY_STRATEGY);
        Optional.ofNullable(strategy).ifPresent( s ->
            builder.withStrategy(new Strategy(strategy.getProperty("name"), strategy.getProperty("parameters")))
        );

        builder.withSteps(
            buildFunctionalStepsChildren(vertex, dbSession)
        );

        ofNullable(parameters).ifPresent(builder::addDataSet);

        return builder;
    }

    static List<FunctionalStep> buildFunctionalStepsChildren(OVertex vertex, ODatabaseSession dbSession) {
        return StreamSupport
            .stream(vertex.getEdges(ODirection.OUT, GE_STEP_CLASS).spliterator(), false)
            .filter(childEdge -> {
                Optional<OVertex> to = ofNullable(childEdge.getTo());
                if (!to.isPresent()) {
                    LOGGER.warn("Ignoring edge {} with no to vertex", childEdge);
                }
                return to.isPresent();
            })
            .map(childEdge -> {
                FunctionalStep.FunctionalStepBuilder childBuilder = vertexToFunctionalStep(childEdge.getTo(), dbSession);
                overwriteDataSetWithEdgeParameters(childEdge, childBuilder);
                return childBuilder.build();
            })
            .collect(Collectors.toList());
    }

    private static void overwriteDataSetWithEdgeParameters(OEdge childEdge, FunctionalStep.FunctionalStepBuilder builder) {
        Optional.<Map<String, String>>ofNullable(
            childEdge.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS)
        ).ifPresent(builder::addDataSet);
    }

    private static Map<String, String> vertexToDataSet(final OVertex vertex, final ODatabaseSession dbSession) {
        reloadIfDirty(vertex);
        Map<String, String> dataSet = mergeFunctionalStepsChildrenDataSets(vertex, dbSession);
        Map<String, String> parameters = vertex.getProperty(STEP_CLASS_PROPERTY_PARAMETERS);
        ofNullable(parameters)
            .ifPresent(dataSet::putAll);
        return dataSet;
    }

    private static Map<String, String> mergeFunctionalStepsChildrenDataSets(OVertex vertex, ODatabaseSession dbSession) {
        return StreamSupport
            .stream(vertex.getEdges(ODirection.OUT, GE_STEP_CLASS).spliterator(), false)
            .map(childEdge -> {
                Map<String, String> dataSet = vertexToDataSet(childEdge.getTo(), dbSession);
                Optional.<Map<String, String>>ofNullable(
                    childEdge.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS)
                ).ifPresent(dataSet::putAll);
                return dataSet;
            })
            .reduce(new HashMap<>(), (m1, m2) -> {
                m1.putAll(m2);
                return m1;
            });
    }
}
