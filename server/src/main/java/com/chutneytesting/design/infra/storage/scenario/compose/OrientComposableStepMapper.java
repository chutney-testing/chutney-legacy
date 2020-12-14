package com.chutneytesting.design.infra.storage.scenario.compose;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_RANK;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_IMPLEMENTATION;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_NAME;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_STRATEGY;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_USAGE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.load;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.reloadIfDirty;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.setOrRemoveProperty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.StepUsage;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
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

public class OrientComposableStepMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientComposableStepMapper.class);

    static void composableStepToVertex(final ComposableStep composableStep, OVertex step, ODatabaseSession dbSession) {
        step.setProperty(STEP_CLASS_PROPERTY_NAME, composableStep.name, OType.STRING);
        setOrRemoveProperty(step, STEP_CLASS_PROPERTY_USAGE, composableStep.usage, OType.STRING);
        setOrRemoveProperty(step, STEP_CLASS_PROPERTY_IMPLEMENTATION, composableStep.implementation, OType.STRING);
        setOrRemoveProperty(step, STEP_CLASS_PROPERTY_TAGS, composableStep.tags, OType.EMBEDDEDLIST);

        OElement strategy = dbSession.newElement();
        strategy.setProperty("name", composableStep.strategy.type, OType.STRING);
        strategy.setProperty("parameters", composableStep.strategy.parameters, OType.EMBEDDEDMAP);

        setOrRemoveProperty(step, STEP_CLASS_PROPERTY_STRATEGY, strategy, OType.EMBEDDED);
        step.setProperty(STEP_CLASS_PROPERTY_PARAMETERS, composableStep.builtInParameters, OType.EMBEDDEDMAP);
        setComposableStepVertexDenotations(step, composableStep.steps, dbSession);
    }

    static void setComposableStepVertexDenotations(OVertex vertex, List<ComposableStep> edgesToSave, ODatabaseSession dbSession) {
        vertex.getEdges(ODirection.OUT, GE_STEP_CLASS).forEach(ORecord::delete);
        IntStream.range(0, edgesToSave.size())
            .forEach(index -> {
                final ComposableStep subComposableStepRef = edgesToSave.get(index);
                OVertex dbSubComposableStep = (OVertex) load(subComposableStepRef.id, dbSession)
                    .orElseThrow(() -> new IllegalArgumentException("Composable step with id [" + subComposableStepRef.id + "] does not exists"));
                final Map<String, String> subComposableStepDataSet = vertexToDataSet(dbSubComposableStep, dbSession);
                Map<String, String> parameters = cleanChildOverloadedParametersMap(subComposableStepRef.enclosedUsageParameters, subComposableStepDataSet);

                OEdge childEdge = vertex.addEdge(dbSubComposableStep, GE_STEP_CLASS);
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

    public static ComposableStep.ComposableStepBuilder vertexToComposableStep(final OVertex vertex, final ODatabaseSession dbSession) {
        reloadIfDirty(vertex);

        ComposableStep.ComposableStepBuilder builder = ComposableStep.builder()
            .withId(vertex.getIdentity().toString())
            .withName(vertex.getProperty(STEP_CLASS_PROPERTY_NAME))
            .withTags(vertex.getProperty(STEP_CLASS_PROPERTY_TAGS));

        String usage = vertex.getProperty(STEP_CLASS_PROPERTY_USAGE);
        ofNullable(usage).ifPresent(u -> builder.withUsage(Optional.of(StepUsage.valueOf(u))));

        builder.withImplementation(ofNullable(vertex.getProperty(STEP_CLASS_PROPERTY_IMPLEMENTATION)));

        Map<String, String> parameters = vertex.getProperty(STEP_CLASS_PROPERTY_PARAMETERS);
        ofNullable(parameters).ifPresent(builder::addBuiltInParameters);

        OElement strategy = vertex.getProperty(STEP_CLASS_PROPERTY_STRATEGY);
        Optional.ofNullable(strategy).ifPresent( s ->
            builder.withStrategy(new Strategy(strategy.getProperty("name"), strategy.getProperty("parameters")))
        );

        builder.withSteps(
            buildComposableStepsChildren(vertex, dbSession)
        );

        ofNullable(parameters).ifPresent(builder::addEnclosedUsageParameters);

        return builder;
    }

    static List<ComposableStep> buildComposableStepsChildren(OVertex vertex, ODatabaseSession dbSession) {
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
                ComposableStep.ComposableStepBuilder childBuilder = vertexToComposableStep(childEdge.getTo(), dbSession);
                overwriteDataSetWithEdgeParameters(childEdge, childBuilder);
                return childBuilder.build();
            })
            .collect(Collectors.toList());
    }

    private static void overwriteDataSetWithEdgeParameters(OEdge childEdge, ComposableStep.ComposableStepBuilder builder) {
        Optional.<Map<String, String>>ofNullable(
            childEdge.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS)
        ).ifPresent(builder::addEnclosedUsageParameters);
    }

    private static Map<String, String> vertexToDataSet(final OVertex vertex, final ODatabaseSession dbSession) {
        reloadIfDirty(vertex);
        Map<String, String> dataSet = mergeComposableStepsChildrenDatasets(vertex, dbSession);
        Map<String, String> parameters = vertex.getProperty(STEP_CLASS_PROPERTY_PARAMETERS);
        ofNullable(parameters)
            .ifPresent(dataSet::putAll);
        return dataSet;
    }

    private static Map<String, String> mergeComposableStepsChildrenDatasets(OVertex vertex, ODatabaseSession dbSession) {
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
