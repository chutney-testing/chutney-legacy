package com.chutneytesting.scenario.api.compose.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableStrategy.class)
@JsonDeserialize(as = ImmutableStrategy.class)
@Value.Style(jdkOnly = true)
public interface Strategy {

    @Value.Default()
    default String type() { return "Default"; }

    Map<String, Object> parameters();

}

