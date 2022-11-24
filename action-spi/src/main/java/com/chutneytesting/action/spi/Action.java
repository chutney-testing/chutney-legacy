package com.chutneytesting.action.spi;

import java.util.Collections;
import java.util.List;

/**
 * Marking interface for an executable {@link Action}.<br>
 * A action implementation will be instantiated for each execution, so that no state will be kept from one execution to another.<br>
 * <p><b>Warning:</b> A action implementation must have one and only-one constructor</p>
 * <p>
 * Actions are identified by their class name converted from PascalCase to spinal-case.
 * This is done by <code>com.chutneytesting.action.api.ActionTemplateMapper</code>
 * <p>
 * ex. MySuperAction will become my-super-action
 * <p>
 * This spinal-case identifier is used by external systems to specify which actions to execute.
 * For more information, @see com.chutneytesting.action.api package
 */
// TODO add javadoc on constructor possible parameters
public interface Action {

    /**
     * Execute the action.
     *
     * @return a {@link ActionExecutionResult} according to how the execution went
     */
    ActionExecutionResult execute();

    /**
     * @return the errors returned
     */
    default List<String> validateInputs() {
        return Collections.emptyList();
    }
}
