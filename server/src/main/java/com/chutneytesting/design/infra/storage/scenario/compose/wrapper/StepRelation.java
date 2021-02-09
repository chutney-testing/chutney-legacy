package com.chutneytesting.design.infra.storage.scenario.compose.wrapper;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS_PROPERTY_PARAMETERS;
import static java.util.Optional.ofNullable;

import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepRelation {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepRelation.class);

    private final OEdge relation;

    public StepRelation(OEdge relation) {
        this.relation = relation;
    }

    OVertex getParentVertex() {
        return relation.getFrom();
    }

    OVertex getChildVertex() {
        return relation.getTo();
    }

    StepVertex getParentStep() {
        return StepVertex.builder()
            .from(getParentVertex())
            .build();
    }

    StepVertex getChildStep() {
        return StepVertex.builder()
            .from(getChildVertex())
            .withExecutionParameters(relation.getProperty(GE_STEP_CLASS_PROPERTY_PARAMETERS))
            .build();
    }

    public boolean isValid() {
        boolean isValid = ofNullable(getChildVertex()).isPresent();
        if (!isValid) {
            LOGGER.warn("Ignoring edge {} with no child vertex", relation);
        }
        return isValid;
    }
}
