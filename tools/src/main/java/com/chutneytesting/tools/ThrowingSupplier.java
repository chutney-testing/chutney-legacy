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
