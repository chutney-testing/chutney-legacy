package com.chutneytesting.tools.ui;

import java.util.function.Consumer;

/**
 * Version of the {@link Consumer} able to throw Checked {@link Exception Exceptions}.
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {

    void accept(T t) throws Exception;

    /**
     * @param throwing the {@link ThrowingConsumer} to convert to {@link Consumer}
     * @param errorHandler used when an {@link Exception} is raised by the given {@link ThrowingConsumer}
     */
    static <T> Consumer<T> silent(ThrowingConsumer<T> throwing, Consumer<Exception> errorHandler) {
        return t -> {
            try {
                throwing.accept(t);
            } catch (Exception e) {
                errorHandler.accept(e);
            }
        };
    }
}
