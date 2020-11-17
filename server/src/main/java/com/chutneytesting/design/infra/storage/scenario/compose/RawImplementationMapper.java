package com.chutneytesting.design.infra.storage.scenario.compose;

import com.chutneytesting.execution.domain.compiler.ScenarioConversionException;
import com.chutneytesting.task.api.EmbeddedTaskEngine;
import com.chutneytesting.task.api.TaskDto;
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
import org.apache.commons.lang3.StringUtils;

class RawImplementation {

    private final EmbeddedTaskEngine embeddedTaskEngine;
    private final ObjectMapper objectMapper;

    private RawImplementation(EmbeddedTaskEngine embeddedTaskEngine, ObjectMapper objectMapper) {
         this.embeddedTaskEngine = embeddedTaskEngine;
         this.objectMapper = objectMapper;
    }

    static RawImplementationBuilder builder() {
        return new RawImplementationBuilder();
    }

    static class RawImplementationBuilder {

        private JsonNode implementation;

        public RawImplementationBuilder from(String rawImplementation) {
            try {
                this.implementation = objectMapper.readTree(rawImplementation);
            } catch (IOException e) {
                throw new ScenarioConversionException(e);
            }
            return this;
        }

        String targetName() {
            return Optional.ofNullable(implementation.get("target")).orElse(TextNode.valueOf("")).textValue();
        }

        String type() {
            if (implementation.hasNonNull("identifier")) {
                return implementation.get("identifier").textValue();
            }
            return null;
        }

        Map<String, Object> outputs() {
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

        Map<String, Object> inputs() {
            Map<String, Object> inputs = new LinkedHashMap<>();
            // Simple inputs
            if (implementation.hasNonNull("inputs")) {
                final JsonNode simpleInputs = implementation.get("inputs");
                simpleInputs.forEach(in -> {
                    String inputName = in.get("name").asText();
                    inputs.put(inputName, transformSimpleInputValue(in, inputName));
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

        private Object transformSimpleInputValue(JsonNode in, String inputRead) {
            Optional<TaskDto> task = embeddedTaskEngine.getAllTasks().stream().filter(t -> t.getIdentifier().equals(this.type())).findFirst();
            if (task.isPresent()) {
                Optional<TaskDto.InputsDto> optionalInput = task.get().getInputs().stream().filter(i -> i.getName().equals(inputRead)).findFirst();
                if (optionalInput.isPresent()) {
                    TaskDto.InputsDto input = optionalInput.get();
                    if (input.getType().getName().equals(Integer.class.getName())) {
                        return transformIntegerValue(in);
                    }
                }
            }

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

        private Integer transformIntegerValue(JsonNode in) {
            String value = in.get("value").asText();
            return StringUtils.isNotBlank(value) ? Integer.valueOf(value) : null;
        }
    }
}
