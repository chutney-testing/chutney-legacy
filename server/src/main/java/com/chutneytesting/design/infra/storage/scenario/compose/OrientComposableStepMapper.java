package com.chutneytesting.design.infra.storage.scenario.compose;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.design.infra.storage.scenario.compose.wrapper.StepVertex;
import com.orientechnologies.orient.core.db.ODatabaseSession;
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
            .withTags(composableStep.tags)
            .withImplementation(composableStep.implementation)
            .withStrategy(composableStep.strategy)
            .withDefaultParameters(composableStep.defaultParameters)
            .withExecutionParameters(composableStep.executionParameters)
            .withSteps(composableStep.steps)
            .build();
    }

    // GET
    public static ComposableStep.ComposableStepBuilder vertexToComposableStep(final StepVertex vertex) {
        vertex.reloadIfDirty();

        ComposableStep.ComposableStepBuilder builder = ComposableStep.builder()
            .withId(vertex.id())
            .withName(vertex.name())
            .withTags(vertex.tags())
            .withImplementation(vertex.implementation());


        Map<String, String> parameters = vertex.parameters();
        ofNullable(parameters).ifPresent(builder::addDefaultParameters);

        OElement strategy = vertex.strategy();
        Optional.ofNullable(strategy).ifPresent( s ->
            builder.withStrategy(new Strategy(strategy.getProperty("name"), strategy.getProperty("parameters")))
        );

        builder.withSteps(
            buildComposableStepsChildren(vertex)
        );

        ofNullable(parameters).ifPresent(builder::addExecutionParameters); // TODO - should no be done here

        return builder;
    }

    // GET
    static List<ComposableStep> buildComposableStepsChildren(StepVertex vertex) {
        return StreamSupport
            .stream(vertex.getChildren().spliterator(), false)
            .filter(childEdge -> {
                Optional<OVertex> to = ofNullable(childEdge.getTo());
                if (!to.isPresent()) {
                    LOGGER.warn("Ignoring edge {} with no to vertex", childEdge);
                }
                return to.isPresent();
            })
            .map(childEdge -> {
                ComposableStep.ComposableStepBuilder childBuilder = vertexToComposableStep(StepVertex.builder().from(childEdge.getTo()).build());
                overwriteDataSetWithEdgeParameters(childEdge, childBuilder);
                return childBuilder.build();
            })
            .collect(Collectors.toList());
    }

    // GET
    private static void overwriteDataSetWithEdgeParameters(OEdge childEdge, ComposableStep.ComposableStepBuilder builder) {
        Optional.<Map<String, String>>ofNullable(
            childEdge.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS)
        ).ifPresent(builder::addExecutionParameters);
    }

}
