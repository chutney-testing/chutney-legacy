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

import com.chutneytesting.action.spi.Action;
import java.util.Collection;
import java.util.Optional;

/**
 * Registry for {@link ActionTemplate}.
 */
public interface ActionTemplateRegistry {

    /**
     * Refresh all available {@link ActionTemplate} based on given {@link ActionTemplateLoader}.<br>
     * Main use case, except for initialization, is when {@link Action} classes are added to the classpath at runtime.
     */
    void refresh();

    /**
     * @return a {@link ActionTemplate} or empty if the given type did not matched any registered {@link ActionTemplate}
     */
    Optional<ActionTemplate> getByIdentifier(String identifier);

    Collection<ActionTemplate> getAll();
}
