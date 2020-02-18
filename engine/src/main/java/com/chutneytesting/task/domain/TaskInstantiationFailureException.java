package com.chutneytesting.task.domain;

import java.util.List;

/**
 * Thrown when a task fails to be instantiated.
 *
 * @see TaskTemplate#create(List)
 */
public class TaskInstantiationFailureException extends RuntimeException {

    public TaskInstantiationFailureException(String taskIdentifier, ReflectiveOperationException cause) {
        super("Unable to instantiate Task[" + taskIdentifier + "]: " + cause.getMessage(), cause);
    }

}
