package com.chutneytesting.design.infra.storage.scenario.compose;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.design.infra.storage.scenario.compose.wrapper.StepRelation;
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
    public static ComposableStep vertexToComposableStep(final StepVertex vertex) {
        return altVertexToComposableStep(vertex).build();
    }

    // GET
    private static ComposableStep.ComposableStepBuilder altVertexToComposableStep(final StepVertex vertex) {
        vertex.reloadIfDirty();

        ComposableStep.ComposableStepBuilder builder = ComposableStep.builder()
            .withId(vertex.id())
            .withName(vertex.name())
            .withTags(vertex.tags())
            .withImplementation(vertex.implementation())
            .withDefaultParameters(vertex.defaultParameters())
            .withExecutionParameters(vertex.executionParameters());

        OElement strategy = vertex.strategy();
        Optional.ofNullable(strategy).ifPresent( s ->
            builder.withStrategy(new Strategy(strategy.getProperty("name"), strategy.getProperty("parameters")))
        );

        /*List<ComposableStep> composableSteps = altBuildComposableStepsChildren(vertex);
        List<ComposableStep> composableSteps_2 = newBuild(vertex);*/
        List<ComposableStep> composableSteps_3 = anotherBuild(vertex);

        /*if (!composableSteps.equals(composableSteps_2)) {
            throw new IllegalStateException();
        }

        if (!composableSteps_2.equals(composableSteps_3)) {
            throw new IllegalStateException();
        }*/

        builder.withSteps(
            composableSteps_3
        );

        return builder;
    }

    static List<ComposableStep> anotherBuild(StepVertex vertex) {
        return vertex.listChildrenSteps().stream()
            .map(OrientComposableStepMapper::vertexToComposableStep)
            .collect(Collectors.toList());
    }
/*
    // GET
    static List<ComposableStep> altBuildComposableStepsChildren(StepVertex vertex) {
        List<ComposableStep> collect = StreamSupport
            .stream(vertex.getChildrenEdges().spliterator(), false)
            .filter(childEdge -> {
                Optional<OVertex> to = ofNullable(childEdge.getTo());
                if (!to.isPresent()) {
                    LOGGER.warn("Ignoring edge {} with no to vertex", childEdge);
                }
                return to.isPresent();
            })
            .map(childEdge -> {
                StepVertex build = StepVertex.builder().from(childEdge.getTo()).build();
                ComposableStep.ComposableStepBuilder childBuilder = altVertexToComposableStep(build);
                overwriteDataSetWithEdgeParameters(childEdge, childBuilder);
                return childBuilder.build();
            })
            .collect(Collectors.toList());

        return collect;
    }

    // GET
    private static void overwriteDataSetWithEdgeParameters(OEdge childEdge, ComposableStep.ComposableStepBuilder builder) {
        Optional.<Map<String, String>>ofNullable(
            childEdge.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS)
        ).ifPresent(builder::withExecutionParameters);
    }

    static List<ComposableStep> newBuild(StepVertex vertex) {
        List<StepRelation> stepRelations = vertex.listValidChildrenEdges();


        List<ComposableStep> collect = stepRelations.stream()
            .map(relation -> {
                StepVertex build = StepVertex.builder()
                    .from(relation.getChildVertex())
                    .build();

                StepVertex childStep = relation.getChildStep();

                ComposableStep.ComposableStepBuilder childBuilder = altVertexToComposableStep(build)
                    .withExecutionParameters(relation.executionParameters());
                return childBuilder.build();
            })
            .collect(Collectors.toList());

        List<ComposableStep> collect_2 = stepRelations.stream()
            .map(relation -> StepVertex.builder()
                .from(relation.getChildVertex())
                .withExecutionParameters(relation.executionParameters())
                .build()
            )
            .map(OrientComposableStepMapper::vertexToComposableStep)
            .collect(Collectors.toList());

        if (!collect.equals(collect_2)) {
            //throw new IllegalStateException();
        }

        return collect;
    }*/

    // GET
/*    static List<ComposableStep> altBuildComposableStepsChildren_2(StepVertex vertex) {
        return vertex.getValidChildrenEdges().stream()
            .map(childEdge -> {
                Map<String, String> executionParameters = childEdge.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS);
                StepVertex.builder().from(childEdge.getTo()).build();
                ComposableStep.ComposableStepBuilder childBuilder = altVertexToComposableStep();
                //overwriteDataSetWithEdgeParameters(childEdge, childBuilder);
                return childBuilder.build();
            })
            .collect(Collectors.toList());
    }

    // GET
    static List<ComposableStep> altBuildComposableStepsChildren_3(StepVertex vertex) {

        List<ComposableStep> collect = vertex.getChildrenVertice()
            .stream()
            .map(childVertex -> altVertexToComposableStep(childVertex))
            //.peek(b -> )
            .collect(Collectors.toList());

        return collect;
    }*/
}
