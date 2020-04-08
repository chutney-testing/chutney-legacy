package com.chutneytesting.task.spi;

/**
 * Marking interface for an executable {@link Task}.<br>
 * A task implementation will be instantiated for each execution, so that no state will be kept from one execution to another.<br>
 * <p>
 * <font color="red"><b>Warning:</b> A task implementation must have one and only-one constructor</font>
 * </p>
 *
 * Tasks are identified by their class name converted from PascalCase to spinal-case.
 * This is done by {@link com.chutneytesting.task.api.TaskTemplateMapper}
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
}
