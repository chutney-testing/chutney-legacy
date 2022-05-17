package com.chutneytesting.scenario.infra.compose;

import com.chutneytesting.execution.domain.compiler.ScenarioConversionException;
import com.chutneytesting.execution.domain.scenario.composed.StepImplementation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RawImplementationMapper {

    private final ObjectMapper objectMapper;

    public RawImplementationMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public StepImplementation deserialize(String rawImplementation) {
        JsonNode implementation;

        try {
            implementation = objectMapper.readTree(rawImplementation);
        } catch (IOException e) {
            throw new ScenarioConversionException(e);
        }

        return new StepImplementation(
            type(implementation),
            target(implementation),
            inputs(implementation),
            outputs(implementation),
            validations(implementation)
        );
    }

    private String type(JsonNode implementation) {
        if (implementation.hasNonNull("identifier")) {
            return implementation.get("identifier").textValue();
        }
        return null;
    }

    private String target(JsonNode implementation) {
        return Optional.ofNullable(implementation.get("target")).orElse(TextNode.valueOf("")).textValue();
    }

    private Map<String, Object> outputs(JsonNode implementation) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        if (implementation.hasNonNull("outputs")) {
            final JsonNode outputsNode = implementation.get("outputs");
            outputsNode.forEach(in -> {
                String name = in.get("key").asText();
                outputs.put(name, in.get("value").asText());
            });
        }
        return outputs;
    }

    private Map<String, Object> inputs(JsonNode implementation) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        // Simple inputs
        if (implementation.hasNonNull("inputs")) {
            final JsonNode simpleInputs = implementation.get("inputs");
            simpleInputs.forEach(in -> {
                String inputName = in.get("name").asText();
                inputs.put(inputName, transformSimpleInputValue(in));
            });
        }
        // List inputs
        if (implementation.hasNonNull("listInputs")) {
            final JsonNode listInputs = implementation.get("listInputs");
            listInputs.forEach(in -> {
                List<Object> values = new ArrayList<>();
                in.get("values").forEach(v -> values.add(transformListInputValue(v)));
                inputs.put(in.get("name").asText(), values);
            });
        }
        // Map inputs
        if (implementation.hasNonNull("mapInputs")) {
            final JsonNode mapInputs = implementation.get("mapInputs");
            mapInputs.forEach(in -> {
                LinkedHashMap<String, String> values = new LinkedHashMap<>();
                for (JsonNode next : in.get("values")) {
                    values.put(next.get("key").asText(), next.get("value").asText());
                }
                inputs.put(in.get("name").asText(), values);
            });
        }
        return inputs;
    }

    private Map<String, Object> validations(JsonNode implementation) {
        Map<String, Object> validations = new LinkedHashMap<>();
        if (implementation.hasNonNull("validations")) {
            final JsonNode validationsNode = implementation.get("validations");
            validationsNode.forEach(in -> {
                String name = in.get("key").asText();
                validations.put(name, in.get("value").asText());
            });
        }
        return validations;
    }

    private Object transformSimpleInputValue(JsonNode in) {
        String value = in.get("value").asText();
        return !value.isEmpty() ? value : null;
    }

    private Object transformListInputValue(JsonNode in) {
        if (in.isObject()) {
            try {
                return objectMapper.readValue(in.toString(), HashMap.class);
            } catch (Exception e) {
                return in.toString();
            }
        }
        return in.asText();
    }
}
