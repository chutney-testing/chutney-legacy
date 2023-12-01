/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.engine.domain.execution.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.injectable.Target;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class FinallyActionMapperTest {

    private final FinallyActionMapper mapper = new FinallyActionMapper();

    @Test
    public void upright_finally_action_copy() {
        FinallyAction finallyAction = FinallyAction.Builder
            .forAction("test-action", "action name")
            .withTarget(TargetImpl.builder()
                .withName("test-target")
                .withUrl("proto://host:12345")
                .build())
            .withInput("test-input", "test")
            .withValidation("test-validation", true)
            .withStrategyType("strategyType")
            .withStrategyProperties(Map.of("param", "value"))
            .build();

        StepDefinition stepDefinition = mapper.toStepDefinition(finallyAction, "env");

        assertThat(stepDefinition.type).isEqualTo("test-action");
        assertThat(stepDefinition.name).isEqualTo("action name");
        assertThat(stepDefinition.inputs()).containsOnly(entry("test-input", "test"));
        assertThat(stepDefinition.validations).containsOnly(entry("test-validation", true));
        assertThat(stepDefinition.getTarget()).isPresent();
        Target targetCopy = stepDefinition.getTarget().get();
        assertThat(targetCopy.name()).isEqualTo("test-target");
        assertThat(targetCopy.uri().toString()).isEqualTo("proto://host:12345");
        assertThat(targetCopy.user()).isEmpty();
        assertThat(targetCopy.userPassword()).isEmpty();
        assertThat(stepDefinition.getStrategy()).hasValueSatisfying(s -> {
            assertThat(s.type).isEqualTo("strategyType");
            assertThat(s.strategyProperties).contains(entry("param", "value"));
        });
        assertThat(stepDefinition.environment).isEqualTo("env");
    }
}
