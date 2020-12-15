package com.chutneytesting.design.infra.storage.scenario.compose.dto;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.load;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.reloadIfDirty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.compose.ComposableStepNotFoundException;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class StepVertex {

    public final OVertex vertex;

    private StepVertex(OVertex vertex) {
        this.vertex = vertex;
    }

    public Map<String, String> getDataset() {
        reloadIfDirty(vertex);
        Map<String, String> dataset = mergeComposableStepsChildrenDatasets();
        Map<String, String> parameters = vertex.getProperty(STEP_CLASS_PROPERTY_PARAMETERS);
        ofNullable(parameters).ifPresent(dataset::putAll);
        return dataset;
    }

    private Map<String, String> mergeComposableStepsChildrenDatasets() {
        return StreamSupport
            .stream(vertex.getEdges(ODirection.OUT, GE_STEP_CLASS).spliterator(), false)
            .map(childEdge -> {
                StepVertex subStepVertex = StepVertex.builder()
                    .from(childEdge.getTo())
                    .build();
                Map<String, String> dataSet = subStepVertex.getDataset();
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

    public static StepVertexBuilder builder() {
        return new StepVertexBuilder();
    }

    public static class StepVertexBuilder {

        String id;
        ODatabaseSession dbSession;

        OVertex vertex;

        private StepVertexBuilder() {};

        public StepVertex build() {
            if (this.vertex == null) {
                this.vertex = (OVertex) load(id, dbSession).orElseThrow(() -> new ComposableStepNotFoundException(id));
            }

            return new StepVertex(vertex);
        }

        public StepVertexBuilder from(OVertex vertex) {
            this.vertex = vertex;
            return this;
        }

        public StepVertexBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public StepVertexBuilder usingSession(ODatabaseSession dbSession) {
            this.dbSession = dbSession;
            return this;
        }
    }
}
