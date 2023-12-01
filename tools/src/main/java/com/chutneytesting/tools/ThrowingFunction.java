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
