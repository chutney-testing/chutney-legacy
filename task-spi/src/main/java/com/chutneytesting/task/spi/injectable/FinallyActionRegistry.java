package com.chutneytesting.task.spi.injectable;

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.Task;

/**
 * Registry to declare at <i>execution-time</i> a {@link FinallyAction}.
 *
 * @see FinallyAction
 */
@FunctionalInterface
public interface FinallyActionRegistry {

    /**
     * This methods is used to register a {@link FinallyAction} during the execution, as a
     * {@link Task} may have to free resources after execution.
     * <p>
     * Such registration is effective only if the execution reaches the {@link Task}
     * that defines it.
     *
     * @param finallyAction to be executed after all steps defined in a Scenario
     */
    void registerFinallyAction(FinallyAction finallyAction);
}
