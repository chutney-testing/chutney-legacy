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

import com.chutneytesting.action.domain.ActionTemplateRegistry;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmbeddedActionEngine {

    private final List<ActionDto> allActions;

    public EmbeddedActionEngine(ActionTemplateRegistry actionTemplateRegistry) {
        this.allActions = actionTemplateRegistry.getAll().parallelStream()
            .map(ActionTemplateMapper::toDto)
            .collect(Collectors.toList());
    }

    public List<ActionDto> getAllActions() {
        return allActions;
    }

    public Optional<ActionDto> getAction(String identifier) {
        return this.allActions.stream()
            .filter(actionDto -> actionDto.getIdentifier().equals(identifier))
            .findFirst();
    }
}
