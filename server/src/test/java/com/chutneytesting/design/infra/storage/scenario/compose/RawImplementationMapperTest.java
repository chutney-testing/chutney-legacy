package com.chutneytesting.design.infra.storage.scenario.compose;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.execution.domain.scenario.composed.StepImplementation;
import com.chutneytesting.task.api.EmbeddedTaskEngine;
import com.chutneytesting.task.api.TaskDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Lists;
import org.junit.Test;

public class RawImplementationMapperTest {

    private final EmbeddedTaskEngine embeddedTaskEngine = mock(EmbeddedTaskEngine.class);
    private final ObjectMapper om = new WebConfiguration().objectMapper();
    private final RawImplementationMapper sut = new RawImplementationMapper(embeddedTaskEngine, om);

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

        when(embeddedTaskEngine.getAllTasks()).thenReturn(Lists.list(
            new TaskDto("task-id", false, Lists.list(
                new TaskDto.InputsDto("simpleInputName", Integer.class),
                new TaskDto.InputsDto("listInputName", List.class),
                new TaskDto.InputsDto("mapInputName", Map.class)
            ))
        ));

        StepImplementation deserialize = sut.deserialize(implementation);

        assertThat(deserialize.inputs).contains(
            entry("simpleInputName", paramRefString),
            entry("listInputName", Lists.list(paramRefString)),
            entry("mapInputName", Maps.of(paramRefString, paramRefString))
        );
    }
}
