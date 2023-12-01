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
