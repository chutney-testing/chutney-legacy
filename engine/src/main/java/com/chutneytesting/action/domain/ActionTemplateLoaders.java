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

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Collection of loaders, allow to get all {@link ActionTemplate} in order, respecting each {@link ActionTemplateLoader} precedence.
 */
public class ActionTemplateLoaders {

    private final List<ActionTemplateLoader> loaders;

    public ActionTemplateLoaders(List<ActionTemplateLoader> loaders) {
        this.loaders = loaders;
    }

    List<ActionTemplate> orderedTemplates() {
        return loaders
            .stream()
            .flatMap(delegate -> delegate.load().stream())
            .collect(Collectors.toList());
    }
}
