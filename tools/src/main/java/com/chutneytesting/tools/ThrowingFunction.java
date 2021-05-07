package com.chutneytesting.tools;

import java.util.function.Function;

/**
 * Version of {@link Function} throwing {@link Exception}.
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {

    R apply(T t) throws E;

    /**
     * @throws UncheckedException if given {@link ThrowingFunction} throws
     */
    static <T, R, E extends Exception> Function<T, R> toUnchecked(ThrowingFunction<T, R, E> throwingFunction) throws UncheckedException {
        return silence(throwingFunction, e -> {
            throw UncheckedException.throwUncheckedException(e);
        });
    }

    static <T, R, E extends Exception> Function<T, R> silence(ThrowingFunction<T, R, E> throwingFunction, Function<Exception, R> exceptionHandler) {
        return t -> {
            try {
                return throwingFunction.apply(t);
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }
}
