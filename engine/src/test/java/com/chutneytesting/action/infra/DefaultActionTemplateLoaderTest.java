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

package com.chutneytesting.action.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestActionTemplateFactory.TestAction;
import com.chutneytesting.action.TestActionTemplateFactory.TestAction3;
import com.chutneytesting.action.domain.ParsingError;
import com.chutneytesting.action.domain.ResultOrError;
import com.chutneytesting.action.domain.ActionInstantiationFailureException;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.ActionTemplateParser;
import com.chutneytesting.action.domain.UnresolvableActionParameterException;
import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.domain.parameter.ParameterResolver;
import com.chutneytesting.action.spi.Action;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class DefaultActionTemplateLoaderTest {

    @Test
    public void load_from_test_file() {
        DefaultActionTemplateLoader<TestAction> actionTemplateLoader = new DefaultActionTemplateLoader<>(
            "test.actions",
            TestAction.class,
            new TestActionTemplateParser());

        assertThat(actionTemplateLoader.load())
            .as("Loaded ActionTemplates")
            .hasSize(2)
            .extracting(ActionTemplate::identifier).containsExactlyInAnyOrder("TestAction1", "TestAction2");

    }

    static class TestActionTemplateParser implements ActionTemplateParser<TestAction> {

        @Override
        public ResultOrError<ActionTemplate, ParsingError> parse(Class<? extends TestAction> actionClass) {
            if (TestAction3.class.equals(actionClass)) {
                return ResultOrError.error(new ParsingError(actionClass, "test error"));
            }
            ActionTemplate actionTemplate = new ActionTemplate() {

                @Override
                public String identifier() {
                    return actionClass.getSimpleName();
                }

                @Override
                public Class<?> implementationClass() {
                    return actionClass;
                }

                @Override
                public Set<Parameter> parameters() {
                    return Collections.emptySet();
                }

                @Override
                public Action create(List<ParameterResolver> parameterResolvers) throws UnresolvableActionParameterException, ActionInstantiationFailureException {
                    throw new RuntimeException(TestAction.class.getSimpleName() + "s are not instantiable");
                }
            };
            return ResultOrError.result(actionTemplate);
        }
    }
}
