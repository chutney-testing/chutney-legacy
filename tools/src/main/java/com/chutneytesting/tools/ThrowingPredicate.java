package com.chutneytesting.tools;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Version of {@link Predicate} throwing {@link Exception}.
 */
@FunctionalInterface
public interface ThrowingPredicate<T, E extends Exception> {

    boolean test(T t) throws E;

    /**
     * @throws UncheckedException if given {@link ThrowingPredicate} throws
     */
    static <T, E extends Exception> Predicate<T> toUnchecked(ThrowingPredicate<T, E> throwingFunction) throws UncheckedException {
        return silence(throwingFunction, e -> {
            throw UncheckedException.throwUncheckedException(e);
        });
    }

    static <T, E extends Exception> Predicate<T> silence(ThrowingPredicate<T, E> throwingFunction, Function<Exception, Boolean> exceptionHandler) {
        return t -> {
            try {
                return throwingFunction.test(t);
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }
}
