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

import static com.chutneytesting.action.TestActionTemplateFactory.buildActionTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestActionTemplateFactory.TestAction1;
import com.chutneytesting.action.TestActionTemplateFactory.TestAction2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ActionTemplateLoadersTest {

    @Test
    public void load_actions_in_order() {
        List<ActionTemplateLoader> loaders = new ArrayList<>();
        loaders.add(() -> Collections.singletonList(buildActionTemplate("action1", TestAction1.class)));
        loaders.add(() -> Collections.singletonList(buildActionTemplate("action2", TestAction2.class)));
        ActionTemplateLoaders actionTemplateLoaders = new ActionTemplateLoaders(loaders);

        assertThat(actionTemplateLoaders.orderedTemplates())
            .as("ActionTemplates from ActionTemplateLoaders")
            .hasSize(2)
            .extracting(ActionTemplate::identifier)
            .containsExactly("action1", "action2");
    }
}
