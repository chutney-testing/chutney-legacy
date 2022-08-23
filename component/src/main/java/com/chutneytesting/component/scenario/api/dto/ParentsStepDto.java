package com.chutneytesting.component.scenario.api.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableParentsStepDto.class)
@JsonDeserialize(as = ImmutableParentsStepDto.class)
@Value.Style(jdkOnly = true)
public interface ParentsStepDto {

    List<NameIdDto> parentSteps();

    List<NameIdDto> parentScenario();

    @Value.Immutable
    @JsonSerialize(as = ImmutableNameIdDto.class)
    @JsonDeserialize(as = ImmutableNameIdDto.class)
    @Value.Style(jdkOnly = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    interface NameIdDto {

        String id();

        String name();
    }
}
