package com.chutneytesting.design.infra.storage.scenario.compose.wrapper;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static java.util.Optional.ofNullable;

import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.HashMap;
import java.util.Map;
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
        Map<String, String> executionParameters = relation.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS);
        return StepVertex.builder()
            .from(getChildVertex())
            .withExecutionParameters(executionParameters)
            .build();
    }

    public Map<String, String> executionParameters() {
        return relation.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS);
    }

    public void setExecutionParameters(Map<String, String> executionParameters) {
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
        Map<String, String> executionParameters = this.executionParameters();
        if (executionParameters != null) {
            Map<String, String> newExecutionParameters = new HashMap<>();
            defaultParameters.forEach((paramKey, paramValue) ->
                newExecutionParameters.put(paramKey, executionParameters.getOrDefault(paramKey, paramValue))
            );
            relation.setProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS, newExecutionParameters);
        }
    }
}
