package com.chutneytesting.cli.infrastruture;

import com.chutneytesting.task.spi.injectable.Target;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    List<Target> targets();
}
