package com.chutneytesting.task.domain;

/**
 * Parse a {@link Class} of type <b>T</b> into a {@link TaskTemplate}.<br>
 * <p>
 * Used in implementations of {@link TaskTemplateLoader}.
 *
 * @param <T> common interface of classes parsable by a {@link TaskTemplateParser}
 */
public interface TaskTemplateParser<T> {

    ResultOrError<TaskTemplate, ParsingError> parse(Class<? extends T> taskClass);
}
