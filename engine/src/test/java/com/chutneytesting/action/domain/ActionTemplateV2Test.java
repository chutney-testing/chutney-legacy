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

package com.chutneytesting.action.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.action.TestActionTemplateFactory.ComplexAction;
import com.chutneytesting.action.TestActionTemplateFactory.Pojo;
import com.chutneytesting.action.TestActionTemplateFactory.TwoParametersAction;
import com.chutneytesting.action.TestActionTemplateFactory.ValidSimpleAction;
import com.chutneytesting.action.TypeBasedParameterResolver;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.ActionExecutionResult.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ActionTemplateV2Test {

    @Test
    public void simple_action_instantiation() {
        ActionTemplate actionTemplate = new ActionTemplateParserV2().parse(ValidSimpleAction.class).result();

        Action action = actionTemplate.create(Collections.emptyList());

        assertThat(action).isExactlyInstanceOf(ValidSimpleAction.class);
    }

    @Test
    public void action_with_parameters_instantiation_and_execution() {
        ActionTemplate actionTemplate = new ActionTemplateParserV2().parse(TwoParametersAction.class).result();
        String stringValue = UUID.randomUUID().toString();

        Action action = actionTemplate.create(Arrays.asList(
            new TypeBasedParameterResolver<>(String.class, p -> stringValue),
            new TypeBasedParameterResolver<>(int.class, p -> 0)
        ));

        assertThat(action).isExactlyInstanceOf(TwoParametersAction.class);

        ActionExecutionResult executionResult = action.execute();

        assertThat(executionResult.status).isEqualTo(Status.Success);
        assertThat(executionResult.outputs).containsOnly(entry("someString", stringValue), entry("someInt", 0));
    }

    @Test
    public void action_with_complex_parameters_instantiation_and_execution() {
        ActionTemplate actionTemplate = new ActionTemplateParserV2().parse(ComplexAction.class).result();
        String stringValue = UUID.randomUUID().toString();
        Pojo pojo = new Pojo("1", "2");
        Action action = actionTemplate.create(Arrays.asList(
            new TypeBasedParameterResolver<>(String.class, p -> stringValue),
            new TypeBasedParameterResolver<>(Pojo.class, p -> pojo)
        ));

        assertThat(action).isExactlyInstanceOf(ComplexAction.class);

        ActionExecutionResult executionResult = action.execute();

        assertThat(executionResult.status).isEqualTo(Status.Success);
        assertThat(executionResult.outputs).containsOnly(entry("someString", stringValue), entry("someObject", pojo));
    }
}
