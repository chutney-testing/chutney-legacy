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
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class DefaultActionTemplateRegistryTest {

    @Test
    public void getByType_returns_matching_actionTemplate() {
        String actionType = "test-type";
        DefaultActionTemplateRegistry actionTemplateRegistry = withActions(buildActionTemplate(actionType, TestAction1.class));

        assertThat(actionTemplateRegistry.getByIdentifier(actionType)).isPresent();
    }

    @Test
    public void getByType_returns_empty_when_no_actionTemplate_matches() {
        String actionType = "test-type";
        DefaultActionTemplateRegistry actionTemplateRegistry = withActions(buildActionTemplate(actionType, TestAction1.class));

        assertThat(actionTemplateRegistry.getByIdentifier("unknown")).isEmpty();
    }

    @Test
    public void registry_keep_the_first_actionTemplate_with_the_same_identifier() {
        String actionType = "test-type";
        ActionTemplate primaryActionTemplate = buildActionTemplate(actionType, TestAction1.class);
        DefaultActionTemplateRegistry actionTemplateRegistry = withActions(primaryActionTemplate, buildActionTemplate(actionType, TestAction2.class));

        assertThat(actionTemplateRegistry.getByIdentifier("test-type")).hasValue(primaryActionTemplate);
    }

    private DefaultActionTemplateRegistry withActions(ActionTemplate... actionTemplates) {
        ActionTemplateLoader actionTemplateLoader = () -> Arrays.asList(actionTemplates);
        return new DefaultActionTemplateRegistry(new ActionTemplateLoaders(Collections.singletonList(actionTemplateLoader)));
    }
}
