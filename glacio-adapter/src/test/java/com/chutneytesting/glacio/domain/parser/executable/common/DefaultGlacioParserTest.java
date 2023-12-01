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

package com.chutneytesting.glacio.domain.parser.executable.common;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.environment.api.EnvironmentApi;
import com.chutneytesting.glacio.domain.parser.executable.DefaultGlacioParser;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.ActionTemplateRegistry;
import com.github.fridujo.glacio.model.Step;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultGlacioParserTest {

    private DefaultGlacioParser sut;

    private ActionTemplateRegistry actionTemplateRegistry;

    @BeforeEach
    public void setUp() {
        actionTemplateRegistry = mock(ActionTemplateRegistry.class);
        final EnvironmentApi environmentEmbeddedApplication = mock(EnvironmentApi.class);
        sut = new DefaultGlacioParser(actionTemplateRegistry, environmentEmbeddedApplication);
    }

    @Test()
    public void should_throw_exception_when_actionid_not_found_in_registry() {
        assertThatThrownBy(() -> sut.parseActionType(buildSimpleStepWithText("actionId")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_take_actionid_as_first_word() {
        String actionId = "succces";
        when(actionTemplateRegistry.getByIdentifier(actionId)).thenReturn(Optional.of(mock(ActionTemplate.class)));
        assertThat(sut.parseActionType(buildSimpleStepWithText(actionId + " it's a step name with actionid delcared")))
            .isEqualTo(actionId);
    }

    @Test
    public void should_take_actionid_as_text_when_only_one_word() {
        String actionIdAsText = "success";
        when(actionTemplateRegistry.getByIdentifier(actionIdAsText)).thenReturn(Optional.of(mock(ActionTemplate.class)));
        assertThat(sut.parseActionType(buildSimpleStepWithText(actionIdAsText)))
            .isEqualTo(actionIdAsText);
    }

    public static Step buildSimpleStepWithText(String stepText) {
        return new Step(false, empty(), stepText, empty(), emptyList());
    }
}
