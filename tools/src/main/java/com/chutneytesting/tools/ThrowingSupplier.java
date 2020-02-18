package com.chutneytesting.tools;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Version of {@link Supplier} throwing {@link Exception}.
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {

    T get() throws E;

    default T unsafeGet(){
        return Try.unsafe(this);
    }

    /**
     * @throws UncheckedException if given {@link ThrowingFunction} throws
     */
    static <T, E extends Exception> Supplier<T> toUnchecked(ThrowingSupplier<T, E> throwingFunction) throws UncheckedException {
        return silence(throwingFunction, e -> {
            throw UncheckedException.throwUncheckedException(e);
        });
    }

    static <T, E extends Exception> Supplier<T> silence(ThrowingSupplier<T, E> throwingFunction, Function<Exception, T> exceptionHandler) {
        return () -> {
            try {
                return throwingFunction.get();
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }
}
