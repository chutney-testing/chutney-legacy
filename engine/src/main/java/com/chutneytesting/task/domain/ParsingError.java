package com.chutneytesting.task.domain;

/**
 * Error produced by a {@link TaskTemplateParser} if the parsing fails.
 */
public class ParsingError {
    private final Class<?> taskClass;
    private final String errorMessage;

    public ParsingError(Class<?> taskClass, String errorMessage) {
        this.taskClass = taskClass;
        this.errorMessage = errorMessage;
    }

    /**
     * @return the class that failed to be parsed
     */
    public Class<?> taskClass() {
        return taskClass;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
