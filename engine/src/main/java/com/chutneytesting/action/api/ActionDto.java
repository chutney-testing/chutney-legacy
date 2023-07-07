package com.chutneytesting.action.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class ActionDto {

    private String identifier;
    private boolean target;
    private List<InputsDto> inputs;

    public ActionDto(String identifier,
                   boolean hasTarget,
                   List<InputsDto> inputs) {
        this.identifier = identifier;
        this.target = hasTarget;
        this.inputs = inputs;
    }

    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty(value="target")
    boolean target() {
        return target;
    }

    public List<InputsDto> getInputs() {
        return inputs;
    }

    public static class InputsDto {

        private String name;
        private Class<?> type;

        public InputsDto(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InputsDto inputsDto = (InputsDto) o;
            return Objects.equals(name, inputsDto.name) &&
                Objects.equals(type, inputsDto.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", InputsDto.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("type=" + type)
                .toString();
        }
    }
}
