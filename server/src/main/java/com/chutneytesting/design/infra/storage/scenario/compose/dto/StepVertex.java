package com.chutneytesting.design.infra.storage.scenario.compose.dto;

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
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepNotFoundException;
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

public class StepVertex {

    private final OVertex vertex;

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

    public void removeAllSubStepReferences() {
        vertex.getEdges(ODirection.OUT, GE_STEP_CLASS).forEach(ORecord::delete);
    }

    public OEdge addSubStep(StepVertex subStep) {
        return this.vertex.addEdge(subStep.vertex, GE_STEP_CLASS);
    }

    public OVertex save() {
        return vertex.save();
    }

    // SAVE
    void setSubStepReferences(List<ComposableStep> subSteps, ODatabaseSession dbSession) {
        this.removeAllSubStepReferences();
        IntStream.range(0, subSteps.size())
            .forEach(index -> {
                final ComposableStep subStep = subSteps.get(index);
                StepVertex subStepVertex = StepVertex.builder().withId(subStep.id).usingSession(dbSession).build();
                final Map<String, String> subStepDataset = subStepVertex.getDataset();
                Map<String, String> parameters = cleanChildOverloadedParametersMap(subStep.enclosedUsageParameters, subStepDataset);

                OEdge childEdge = this.addSubStep(subStepVertex);
                childEdge.setProperty(GE_STEP_CLASS_PROPERTY_RANK, index);
                if (!parameters.isEmpty()) {
                    childEdge.setProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS, parameters, OType.EMBEDDEDMAP);
                }
                childEdge.save();
            });
    }

    // SAVE
    private Map<String, String> cleanChildOverloadedParametersMap(final Map<String, String> instanceDataSet, final Map<String, String> dbDataSet) {
        return instanceDataSet.entrySet().stream()
            .filter(entry -> dbDataSet.containsKey(entry.getKey()))
            .filter(entry -> !dbDataSet.get(entry.getKey()).equals(entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static StepVertexBuilder builder() {
        return new StepVertexBuilder();
    }

    public static class StepVertexBuilder {

        String id;
        ODatabaseSession dbSession;

        OVertex vertex;

        private String name;
        private Optional<StepUsage> usage = empty();
        private Strategy strategy;
        private List<String> tags;
        private Optional<String> implementation = empty();
        private Map<String, String> builtInParameters;
        private Map<String, String> enclosedUsageParameters;
        private List<ComposableStep> steps;

        private StepVertexBuilder() {}

        public StepVertex build() {
            if (this.vertex == null) {
                this.vertex = (OVertex) load(id, dbSession).orElseThrow(() -> new ComposableStepNotFoundException(id));
            }

            ofNullable(name).ifPresent(n -> vertex.setProperty(STEP_CLASS_PROPERTY_NAME, n, OType.STRING));
            usage.ifPresent(u -> setOrRemoveProperty(vertex, STEP_CLASS_PROPERTY_USAGE, u, OType.STRING));
            implementation.ifPresent(i -> setOrRemoveProperty(vertex, STEP_CLASS_PROPERTY_IMPLEMENTATION, i, OType.STRING));
            ofNullable(tags).ifPresent(t -> setOrRemoveProperty(vertex, STEP_CLASS_PROPERTY_TAGS, t, OType.EMBEDDEDLIST));
            ofNullable(builtInParameters).ifPresent( p -> vertex.setProperty(STEP_CLASS_PROPERTY_PARAMETERS, p, OType.EMBEDDEDMAP));

            ofNullable(strategy).ifPresent( s -> {
                OElement strategy = dbSession.newElement();
                strategy.setProperty("name", s.type, OType.STRING);
                strategy.setProperty("parameters", s.parameters, OType.EMBEDDEDMAP);
                setOrRemoveProperty(vertex, STEP_CLASS_PROPERTY_STRATEGY, strategy, OType.EMBEDDED);
            });

            StepVertex stepVertex = new StepVertex(vertex);
            ofNullable(steps).ifPresent(s -> stepVertex.setSubStepReferences(s, dbSession));

            return stepVertex;
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

        public StepVertexBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public StepVertexBuilder withUsage(Optional<StepUsage> usage) {
            this.usage = usage;
            return this;
        }

        public StepVertexBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public StepVertexBuilder withImplementation(Optional<String> implementation) {
            this.implementation = implementation;
            return this;
        }

        public StepVertexBuilder withStrategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public StepVertexBuilder withBuiltInParameters(Map<String, String> builtInParameters) {
            this.builtInParameters = builtInParameters;
            return this;
        }

        public StepVertexBuilder withEnclosedUsageParameters(Map<String, String> enclosedUsageParameters) {
            this.enclosedUsageParameters= enclosedUsageParameters;
            return this;
        }

        public StepVertexBuilder withSteps(List<ComposableStep> steps) {
            this.steps = steps;
            return this;
        }
    }
}
