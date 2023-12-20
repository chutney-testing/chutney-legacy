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

package com.chutneytesting.engine.domain.execution;

import com.chutneytesting.action.TestActionTemplateFactory.FailAction;
import com.chutneytesting.action.TestActionTemplateFactory.SuccessAction;
import com.chutneytesting.action.domain.DefaultActionTemplateRegistry;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.ActionTemplateLoader;
import com.chutneytesting.action.domain.ActionTemplateLoaders;
import com.chutneytesting.action.domain.ActionTemplateParserV2;
import com.chutneytesting.action.domain.ActionTemplateRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Minimal {@link ActionTemplateLoader} with simple actions:
 * <ul>
 * <li>{@link SuccessAction}</li>
 * <li>{@link FailAction}</li>
 * </ul>
 */
public class TestActionTemplateLoader implements ActionTemplateLoader {

    private final List<ActionTemplate> actionTemplates = new ArrayList<>();

    public TestActionTemplateLoader() {
        this.actionTemplates.add(new ActionTemplateParserV2().parse(SuccessAction.class).result());
        this.actionTemplates.add(new ActionTemplateParserV2().parse(FailAction.class).result());
    }

    @Override
    public List<ActionTemplate> load() {
        return actionTemplates;
    }
}
