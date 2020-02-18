package com.chutneytesting.task.spi;

/**
 * Marking interface for an executable {@link Task}.<br>
 * A task implementation will be instantiated for each execution, so that no state will be kept from one execution to another.<br>
 * <p>
 * <font color="red"><b>Warning:</b> A task implementation must have one and only-one constructor</font>
 * </p>
 */
// TODO add javadoc on constructor possible parameters
// TODO add javadoc on identifier deduction
public interface Task {

    /**
     * Execute the task.
     *
     * @return a {@link TaskExecutionResult} according to how the execution went
     */
    TaskExecutionResult execute();
}
