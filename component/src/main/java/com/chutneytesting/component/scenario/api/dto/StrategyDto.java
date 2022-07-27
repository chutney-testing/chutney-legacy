package com.chutneytesting.component.scenario.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableStrategyDto.class)
@JsonDeserialize(as = ImmutableStrategyDto.class)
@Value.Style(jdkOnly = true)
public interface StrategyDto {

    @Value.Default()
    default String type() { return "Default"; }

    Map<String, Object> parameters();

}

