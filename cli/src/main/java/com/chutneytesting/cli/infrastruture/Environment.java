package com.chutneytesting.cli.infrastruture;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEnvironment.class)
@JsonDeserialize(as = ImmutableEnvironment.class)
public interface Environment {

    String name();

    @Value.Default
    default String description() {
        return "";
    }

    @JsonDeserialize(using = TargetJsonDeserializer.class)
    List<TargetImpl> targets();
}
