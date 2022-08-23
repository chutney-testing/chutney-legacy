package com.chutneytesting.tools;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T var1) throws Exception;

    /**
     * @throws UncheckedException if given {@link ThrowingFunction} throws
     */
    static <T> Consumer<T> toUnchecked(ThrowingConsumer<T> throwingConsumer) throws UncheckedException {
        return silence(throwingConsumer, e -> {
            throw UncheckedException.throwUncheckedException(e);
        });
    }

    static <T> Consumer<T> silence(ThrowingConsumer<T> throwingConsumer, Consumer<Exception> exceptionHandler) {
        return t -> {
            try {
                throwingConsumer.accept(t);
            } catch (Exception e) {
                exceptionHandler.accept(e);
            }
        };
    }

}
