package com.chutneytesting.action.domain;

/**
 * Parse a {@link Class} of type <b>T</b> into a {@link ActionTemplate}.<br>
 * <p>
 * Used in implementations of {@link ActionTemplateLoader}.
 *
 * @param <T> common interface of classes parsable by a {@link ActionTemplateParser}
 */
public interface ActionTemplateParser<T> {

    ResultOrError<ActionTemplate, ParsingError> parse(Class<? extends T> actionClass);
}
