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

package com.chutneytesting.action.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.ActionTemplateParserV2;
import com.chutneytesting.action.domain.ActionTemplateRegistry;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EmbeddedActionEngineTest {

    private EmbeddedActionEngine engine ;
    private final ActionTemplateParserV2 parser = new ActionTemplateParserV2();

    @BeforeEach
    public void setUp() {
        // G
        ActionTemplateRegistry registry = Mockito.mock(ActionTemplateRegistry.class);
        List<ActionTemplate> actions = Lists.newArrayList();
        actions.add(parser.parse(TestAction.class).result());
        actions.add(parser.parse(Test2Action.class).result());

        Mockito.when(registry.getAll()).thenReturn(actions);

        this.engine = new EmbeddedActionEngine(registry);
    }

    @Test
    public void getAllActions() {
        // W
        List<ActionDto> allActions = engine.getAllActions();

        // T
        assertThat(allActions).hasSize(2);
        assertThat(allActions.get(0).getIdentifier()).isEqualTo("test");
        assertThat(allActions.get(1).getIdentifier()).isEqualTo("test2");
    }

    @Test
    public void getAction() {
        // W
        Optional<ActionDto> action = engine.getAction("test");

        // T
        assertThat(action).isPresent();
        assertThat(action.get().getIdentifier()).isEqualTo("test");
    }


    private static class TestAction implements Action {
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok();
        }
    }

    private static class Test2Action implements Action {
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok();
        }
    }
}
