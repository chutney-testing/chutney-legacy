package com.chutneytesting.design.infra.storage.scenario.compose.wrapper;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static java.util.Optional.ofNullable;

import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepRelation {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepRelation.class);

    private final OEdge relation;

    public StepRelation(OEdge relation) {
        this.relation = relation;
    }

    public OVertex getParentVertex() {
        return relation.getFrom();
    }

    public StepVertex getParentStep() {
        return StepVertex.builder()
            .from(getParentVertex())
            .build();
    }

    public OVertex getChildVertex() {
        return relation.getTo();
    }

    public StepVertex getChildStep() {
        Map<String, Pair<String, Boolean>> executionParameters = relation.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS);
        return StepVertex.builder()
            .from(getChildVertex())
            .withExecutionParameters(executionParameters)
            .build();
    }

    public Map<String, Pair<String, Boolean>> executionParameters() {
        return relation.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS);
    }

    public void setExecutionParameters(Map<String, Pair<String, Boolean>> executionParameters) {
        relation.setProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS, executionParameters);
    }

    public boolean isValid() {
        boolean isValid = ofNullable(getChildVertex()).isPresent();
        if (!isValid) {
            LOGGER.warn("Ignoring edge {} with no child vertex", relation);
        }
        return isValid;
    }

    public void save() {
        relation.save();
    }

    public void updateExecutionParameters(Map<String, String> defaultParameters) {
        Map<String, Pair<String, Boolean>> executionParameters = this.executionParameters();
        if (executionParameters != null) {
            Map<String, Pair<String, Boolean>> newExecutionParameters = executionParameters.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> updateValue(e, defaultParameters),
                    (v1, v2) -> v1,
                    LinkedHashMap::new
                ));

            relation.setProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS, newExecutionParameters);
        }
    }

    private Pair<String, Boolean> updateValue(Map.Entry<String, Pair<String, Boolean>> entry, Map<String, String> defaultParameters) {
        Boolean isOverride = isOverride(entry, defaultParameters);
        String value = defaultParameters.get(entry.getKey());
        if(isOverride) {
            value = getValue(entry);
        }
        return Pair.of(value, isOverride);
    }

    private String getValue(Map.Entry<String, Pair<String, Boolean>> entry) {
        try {
            return entry.getValue().getLeft();
        }
        catch (ClassCastException e) {
            return String.valueOf(entry.getValue());
        }
    }

    private Boolean isOverride(Map.Entry<String, Pair<String, Boolean>> entry, Map<String, String> defaultParameters) {
        try {
            return entry.getValue().getRight();
        }
        catch (ClassCastException e) {
            String value = String.valueOf(entry.getValue());
            return defaultParameters.containsKey(entry.getKey()) && !defaultParameters.get(entry.getKey()).equals(value);
        }
    }
}
