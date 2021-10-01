package com.chutneytesting.task.spi;

import com.chutneytesting.task.spi.validation.Validator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Marking interface for an executable {@link Task}.<br>
 * A task implementation will be instantiated for each execution, so that no state will be kept from one execution to another.<br>
 * <p><b>Warning:</b> A task implementation must have one and only-one constructor</p>
 *
 * Tasks are identified by their class name converted from PascalCase to spinal-case.
 * This is done by <code>com.chutneytesting.task.api.TaskTemplateMapper</code>
 *
 * ex. MySuperTask will become my-super-task
 *
 * This spinal-case identifier is used by external systems to specify which tasks to execute.
 * For more information, @see com.chutneytesting.task.api package
 */
// TODO add javadoc on constructor possible parameters
public interface Task {

    /**
     * Execute the task.
     *
     * @return a {@link TaskExecutionResult} according to how the execution went
     */
    TaskExecutionResult execute();

    /**
     * @return the errors returned
     */
    default List<String> validateInputs() {
        return Collections.emptyList();
    }
}
