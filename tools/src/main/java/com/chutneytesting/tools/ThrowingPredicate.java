/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
