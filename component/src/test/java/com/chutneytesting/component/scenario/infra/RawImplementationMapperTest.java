package com.chutneytesting.component.scenario.infra;

import static com.chutneytesting.tests.OrientDatabaseHelperTest.objectMapper;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.component.execution.domain.StepImplementation;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class RawImplementationMapperTest {

    private final RawImplementationMapper sut = new RawImplementationMapper(objectMapper());

    @Test
    public void should_map_implementation_keeping_parameter_reference() {
        String paramRefString = "**param**";
        String implementation = """
            {
                "identifier": "action-id",
                "target": "",
                "hasTarget": false,
                "inputs": [
                    {
                        "name": "simpleInputName",
                        "value": "**param**"
                    }
                 ],
                "listInputs": [
                    {
                        "name": "listInputName",
                        "values": ["**param**", "anotherValue"]
                    }
                ],
                "mapInputs": [
                    {
                        "name": "mapInputName",
                        "values": [
                            {
                                "key": "**param**",
                                "value": "**param**"
                            }
                        ]
                    }
                ]
            }
            """;

        StepImplementation deserialize = sut.deserialize(implementation);

        Assertions.assertThat(deserialize.inputs).contains(
            entry("simpleInputName", paramRefString),
            entry("listInputName", Lists.list(paramRefString, "anotherValue")),
            entry("mapInputName", Map.of(paramRefString, paramRefString))
        );
    }
}
