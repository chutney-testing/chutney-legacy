package com.chutneytesting.scenario.infra.compose;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.execution.domain.scenario.composed.StepImplementation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class RawImplementationMapperTest {

    private final ObjectMapper om = new WebConfiguration().objectMapper();
    private final RawImplementationMapper sut = new RawImplementationMapper(om);

    @Test
    public void should_map_implementation_keeping_parameter_reference() {
        String paramRefString = "**param**";
        String implementation = new StringBuilder()
            .append("{")
            .append("\"identifier\": \"task-id\"")
            .append(",")
            .append("\"target\": \"\"")
            .append(",")
            .append("\"hasTarget\": false")
            .append(",")
            .append("\"inputs\": [{\"name\": \"simpleInputName\", \"value\": \""+paramRefString+"\"}]")
            .append(",")
            .append("\"listInputs\": [{\"name\": \"listInputName\", \"values\": [\""+paramRefString+"\", \"anotherValue\"]}]")
            .append(",")
            .append("\"mapInputs\": [{\"name\": \"mapInputName\", \"values\": [{\"key\": \""+paramRefString+"\", \"value\": \""+paramRefString+"\"}]}]")
            .append("}")
            .toString();

        StepImplementation deserialize = sut.deserialize(implementation);

        assertThat(deserialize.inputs).contains(
            entry("simpleInputName", paramRefString),
            entry("listInputName", Lists.list(paramRefString, "anotherValue")),
            entry("mapInputName", Maps.of(paramRefString, paramRefString))
        );
    }
}
