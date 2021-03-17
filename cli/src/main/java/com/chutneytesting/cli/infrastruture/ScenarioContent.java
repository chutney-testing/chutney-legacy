package com.chutneytesting.cli.infrastruture;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@Value.Enclosing
@JsonDeserialize(as = ImmutableScenarioContent.class)
public interface ScenarioContent {

    UnmarshalledStepDefinition scenario();

    @Value.Immutable
    @JsonDeserialize(as = ImmutableScenarioContent.UnmarshalledStepDefinition.class)
    interface UnmarshalledStepDefinition {

        Optional<GwtType> gwtType();

        Optional<String> name();

        Optional<String> target();

        Optional<String> type();

        Optional<UnmarshalledStepStrategyDefinition> strategy();

        Map<String, Object> inputs();

        List<UnmarshalledStepDefinition> steps();

        Map<String, Object> outputs();

        Map<String, Object> validations();

    }

    @Value.Immutable
    @JsonDeserialize(as = ImmutableScenarioContent.UnmarshalledStepStrategyDefinition.class)
    interface UnmarshalledStepStrategyDefinition {

        String type();

        StepDefinitionCore.StrategyPropertiesCore parameters();

        @Value.Derived
        default StepDefinitionCore.StepStrategyDefinitionCore stepStrategyDefinition() {
            return new StepDefinitionCore.StepStrategyDefinitionCore(
                type(),
                parameters()
            );
        }
    }

}
