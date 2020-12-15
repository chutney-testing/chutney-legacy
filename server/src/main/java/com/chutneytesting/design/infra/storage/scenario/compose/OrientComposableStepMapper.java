package com.chutneytesting.design.infra.storage.scenario.compose;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_IMPLEMENTATION;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_NAME;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_STRATEGY;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_USAGE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.reloadIfDirty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.StepUsage;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.design.infra.storage.scenario.compose.dto.StepVertex;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrientComposableStepMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientComposableStepMapper.class);

    // SAVE
    static StepVertex composableStepToVertex(final ComposableStep composableStep, OVertex oVertex, ODatabaseSession dbSession) {
        return StepVertex.builder()
            .from(oVertex)
            .usingSession(dbSession)
            .withName(composableStep.name)
            .withUsage(composableStep.usage)
            .withTags(composableStep.tags)
            .withImplementation(composableStep.implementation)
            .withStrategy(composableStep.strategy)
            .withBuiltInParameters(composableStep.builtInParameters)
            .withEnclosedUsageParameters(composableStep.enclosedUsageParameters)
            .withSteps(composableStep.steps)
            .build();
    }

    // GET
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

    // GET
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

    // GET
    private static void overwriteDataSetWithEdgeParameters(OEdge childEdge, ComposableStep.ComposableStepBuilder builder) {
        Optional.<Map<String, String>>ofNullable(
            childEdge.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS)
        ).ifPresent(builder::addEnclosedUsageParameters);
    }

}
