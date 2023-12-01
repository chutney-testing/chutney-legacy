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

package com.chutneytesting.action.spi.injectable;

import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.Action;

/**
 * Registry to declare at <i>execution-time</i> a {@link FinallyAction}.
 *
 * @see FinallyAction
 */
@FunctionalInterface
public interface FinallyActionRegistry {

    /**
     * This method is used to register a {@link FinallyAction} during the execution, as a
     * {@link Action} may have to free resources after execution.
     * <p>
     * Such registration is effective only if the execution reaches the {@link Action}
     * that defines it.
     *
     * @param finallyAction to be executed after all steps defined in a Scenario
     */
    void registerFinallyAction(FinallyAction finallyAction);
}
