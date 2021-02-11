package com.chutneytesting.design.infra.storage.scenario.compose.wrapper;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_IMPLEMENTATION;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_NAME;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_STRATEGY;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.load;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.setOrRemoveProperty;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepNotFoundException;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class StepVertex {

    private final OVertex vertex;
    private final List<ComposableStep> steps;
    private final Map<String, String> defaultParameters;
    private final Map<String, String> overrideExecutionParameters;

    private StepVertex(OVertex vertex, List<ComposableStep> steps, Map<String, String> defaultParameters, Map<String, String> overrideExecutionParameters) {
        this.vertex = vertex;
        this.steps = steps;
        this.defaultParameters = defaultParameters;
        this.overrideExecutionParameters = overrideExecutionParameters;
    }

    public void reloadIfDirty() {
        OrientUtils.reloadIfDirty(vertex);
    }

    ///// SAVE \\\\\

    public OVertex save(ODatabaseSession dbSession) {
        this.saveParentEdges();
        this.saveChildrenEdges(dbSession);

        return vertex.save();
    }

    private void saveParentEdges() {
        ofNullable(defaultParameters).ifPresent(p -> this.updateExecutionParametersWithParents());
        this.listParentEdges()
            .forEach(StepRelation::save);
    }

    private void updateExecutionParametersWithParents() {
        this.listParentEdges()
            .forEach(relation -> relation.updateExecutionParameters(this.defaultParameters));
    }

    private void saveChildrenEdges(ODatabaseSession dbSession) {
        ofNullable(steps).ifPresent(s -> this.updateSubStepReferences(s, dbSession));
        this.listChildrenEdges()
            .forEach(StepRelation::save);
    }

    ///// Updates children edges - TODO - Next to refactor
    private void updateSubStepReferences(List<ComposableStep> subSteps, ODatabaseSession dbSession) {
        this.removeAllSubStepReferences();

        subSteps.stream()
            .map(subStep ->
                StepVertex.builder()
                    .withId(subStep.id)
                    .usingSession(dbSession)
                    .withExecutionParameters(subStep.executionParameters)
                    .build()
            )
            .forEach(stepVertex -> {

                final Map<String, String> subStepDataset = stepVertex.buildExecutionParameters();
                Map<String, String> executionParameters = cleanChildOverloadedParametersMap(stepVertex.overrideExecutionParameters, subStepDataset);

                OEdge childEdge = this.addSubStep(stepVertex);

                if (!executionParameters.isEmpty()) {
                    childEdge.setProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS, executionParameters, OType.EMBEDDEDMAP);
                }
            });
    }

    private void removeAllSubStepReferences() {
        this.getChildrenEdges().forEach(ORecord::delete);
    }

    private Map<String, String> buildExecutionParameters() {
        this.reloadIfDirty();
        Map<String, String> executionParameters = mergeComposableStepsChildrenExecutionParameters();
        Map<String, String> parameters = vertex.getProperty(STEP_CLASS_PROPERTY_PARAMETERS);
        ofNullable(parameters).ifPresent(executionParameters::putAll);
        return executionParameters;
    }

    private Map<String, String> mergeComposableStepsChildrenExecutionParameters() {
        return StreamSupport
            .stream(getChildrenEdges().spliterator(), false)
            .map(childEdge -> {
                StepVertex currentStep = StepVertex.builder()
                    .from(childEdge.getTo())
                    .build();
                Map<String, String> executionParameters = currentStep.buildExecutionParameters();
                Optional.<Map<String, String>>ofNullable(
                    childEdge.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS)
                ).ifPresent(executionParameters::putAll);
                return executionParameters;
            })
            .reduce(new LinkedHashMap<>(), (m1, m2) -> {
                m1.putAll(m2);
                return m1;
            });
    }

    private Map<String, String> cleanChildOverloadedParametersMap(final Map<String, String> instanceDataSet, final Map<String, String> dbDataSet) {
        return instanceDataSet.entrySet().stream()
            .filter(entry -> dbDataSet.containsKey(entry.getKey()))
            .filter(entry -> !dbDataSet.get(entry.getKey()).equals(entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private OEdge addSubStep(StepVertex subStep) {
        return this.vertex.addEdge(subStep.vertex, GE_STEP_CLASS);
    }

    ///// SAVE END \\\\\

    public List<OVertex> listParentVertices() {
        return StreamSupport
            .stream(vertex.getVertices(ODirection.IN, GE_STEP_CLASS).spliterator(), false)
            .collect(toList());
    }

    public List<OVertex> listChildrenVertices() {
        return StreamSupport
            .stream(vertex.getVertices(ODirection.OUT, GE_STEP_CLASS).spliterator(), false)
            .collect(toList());
    }

    public Iterable<OEdge> getParentEdges() {
        return vertex.getEdges(ODirection.IN, GE_STEP_CLASS);
    }

    public List<StepRelation> listParentEdges() {
        return StreamSupport
            .stream(vertex.getEdges(ODirection.IN, GE_STEP_CLASS).spliterator(), false)
            .map(StepRelation::new)
            .collect(toList());
    }

    public Iterable<OEdge> getChildrenEdges() {
        return vertex.getEdges(ODirection.OUT, GE_STEP_CLASS);
    }

    public List<StepRelation> listChildrenEdges() {
        return StreamSupport
            .stream(vertex.getEdges(ODirection.OUT, GE_STEP_CLASS).spliterator(), false)
            .map(StepRelation::new)
            .collect(toList());
    }

    public List<StepRelation> listValidChildrenEdges() {
        return listChildrenEdges().stream()
            .filter(StepRelation::isValid)
            .collect(toList());
    }

    public List<StepVertex> listChildrenSteps() {
        return this.listValidChildrenEdges().stream()
            .map(StepRelation::getChildStep)
            .collect(toList());
    }

    public String id() {
        return vertex.getIdentity().toString();
    }

    public String name() {
        return vertex.getProperty(STEP_CLASS_PROPERTY_NAME);
    }

    public List<String> tags() {
        return vertex.getProperty(STEP_CLASS_PROPERTY_TAGS);
    }

    public String implementation() {
        return vertex.getProperty(STEP_CLASS_PROPERTY_IMPLEMENTATION);
    }

    public Map<String, String> defaultParameters() {
        return vertex.getProperty(STEP_CLASS_PROPERTY_PARAMETERS);
    }

    public OElement strategy() {
        return vertex.getProperty(STEP_CLASS_PROPERTY_STRATEGY);
    }

    public static StepVertexBuilder builder() {
        return new StepVertexBuilder();
    }

    public Map<String, String> executionParameters() {
        return this.overrideExecutionParameters;
    }

    public static class StepVertexBuilder {

        String id;
        ODatabaseSession dbSession;

        OVertex vertex;

        private String name;
        private Strategy strategy;
        private List<String> tags;
        private Optional<String> implementation = empty();
        private Map<String, String> defaultParameters;
        private Map<String, String> executionParameters;
        private List<ComposableStep> steps;

        private StepVertexBuilder() {}

        public StepVertex build() {
            if (this.vertex == null) {
                this.vertex = (OVertex) load(id, dbSession).orElseThrow(() -> new ComposableStepNotFoundException(id));
            }

            ofNullable(name).ifPresent(n -> vertex.setProperty(STEP_CLASS_PROPERTY_NAME, n, OType.STRING));
            implementation.ifPresent(i -> setOrRemoveProperty(vertex, STEP_CLASS_PROPERTY_IMPLEMENTATION, i, OType.STRING));
            ofNullable(tags).ifPresent(t -> setOrRemoveProperty(vertex, STEP_CLASS_PROPERTY_TAGS, t, OType.EMBEDDEDLIST));
            ofNullable(defaultParameters).ifPresent(p -> vertex.setProperty(STEP_CLASS_PROPERTY_PARAMETERS, p, OType.EMBEDDEDMAP));

            ofNullable(strategy).ifPresent( s -> {
                OElement strategy = dbSession.newElement();
                strategy.setProperty("name", s.type, OType.STRING);
                strategy.setProperty("parameters", s.parameters, OType.EMBEDDEDMAP);
                setOrRemoveProperty(vertex, STEP_CLASS_PROPERTY_STRATEGY, strategy, OType.EMBEDDED);
            });

           return new StepVertex(vertex, steps, defaultParameters, ofNullable(executionParameters).orElse(emptyMap()));
        }

        public StepVertexBuilder from(OVertex vertex) {
            this.vertex = vertex;
            return this;
        }

        public StepVertexBuilder usingSession(ODatabaseSession dbSession) {
            this.dbSession = dbSession;
            return this;
        }

        public StepVertexBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public StepVertexBuilder withName(String name) {
            this.name = name;
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

        public StepVertexBuilder withDefaultParameters(Map<String, String> defaultParameters) {
            this.defaultParameters = defaultParameters;
            return this;
        }

        public StepVertexBuilder withExecutionParameters(Map<String, String> executionParameters) {
            this.executionParameters = executionParameters;
            return this;
        }

        public StepVertexBuilder withSteps(List<ComposableStep> steps) {
            this.steps = steps;
            return this;
        }
    }
}
