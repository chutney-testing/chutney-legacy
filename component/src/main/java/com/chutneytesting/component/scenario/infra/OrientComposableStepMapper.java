package com.chutneytesting.component.scenario.infra;

import com.chutneytesting.component.ComposableIdUtils;
import com.chutneytesting.component.scenario.domain.ComposableStep;
import com.chutneytesting.component.scenario.infra.wrapper.StepVertex;
import com.chutneytesting.server.core.domain.execution.Strategy;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrientComposableStepMapper {

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
            .withSteps(convertIds(composableStep.steps))
            .build();
    }

    private static List<ComposableStep> convertIds(List<ComposableStep> composableSteps) {
        return composableSteps.stream().map(cs -> ComposableStep.builder()
            .from(cs)
            .withId(ComposableIdUtils.toInternalId(cs.id))
            .withSteps(convertIds(cs.steps))
            .withExecutionParameters(cs.executionParameters)
            .build()).collect(Collectors.toList());
    }
    // GET
    public static ComposableStep vertexToComposableStep(final StepVertex vertex) {
        vertex.reloadIfDirty();
        String externalId = ComposableIdUtils.toExternalId(vertex.id());
        ComposableStep.ComposableStepBuilder builder = ComposableStep.builder()
            .withId(externalId)
            .withName(vertex.name())
            .withTags(vertex.tags())
            .withImplementation(vertex.implementation())
            .withDefaultParameters(vertex.defaultParameters())
            .withExecutionParameters(vertex.executionParameters());

        OElement strategy = vertex.strategy();
        Optional.ofNullable(strategy).ifPresent( s ->
            builder.withStrategy(new Strategy(strategy.getProperty("name"), strategy.getProperty("parameters")))
        );

        builder.withSteps(
            vertexToComposableStep(vertex.listChildrenSteps())
        );

        return builder.build();
    }

    public static List<ComposableStep> vertexToComposableStep(List<StepVertex> subSteps) {
        return subSteps.stream()
            .map(OrientComposableStepMapper::vertexToComposableStep)
            .collect(Collectors.toList());
    }

}
