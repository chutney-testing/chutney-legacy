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

package blackbox.action;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;

/**
 * Action registering itself as {@link FinallyAction}.
 * <p>
 * Used in a scenario, this action proves that there is no infinite-loop when a {@link FinallyAction} registers another
 * {@link FinallyAction} with the same identifier
 */
public class SelfRegisteringFinallyAction implements Action {

    private final FinallyActionRegistry finallyActionRegistry;

    public SelfRegisteringFinallyAction(FinallyActionRegistry finallyActionRegistry) {
        this.finallyActionRegistry = finallyActionRegistry;
    }

    @Override
    public ActionExecutionResult execute() {
        finallyActionRegistry.registerFinallyAction(FinallyAction.Builder.forAction("self-registering-finally", SelfRegisteringFinallyAction.class).build());
        return ActionExecutionResult.ok();
    }
}
