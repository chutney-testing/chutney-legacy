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
import java.util.function.Function;

public class Try<T> {

    public static <T, E extends Exception> T unsafe(ThrowingSupplier<T, E> block) {
        return unsafe(null, block);
    }

    public static <T, E extends Exception> T unsafe(String message, ThrowingSupplier<T, E> block) {
        return exec(block).runtime(message);
    }

    public static <T, E extends Exception> Try<T> exec(ThrowingSupplier<T, E> builder) {
        try {
            return new Try<>(builder.get(), null);
        } catch (Exception e) {
            return new Try<>(null, e);
        }
    }

    private final T value;
    private final Exception error;

    private Try(T value, Exception error) {
        this.value = value;
        this.error = error;
    }

    public T runtime() {
        return runtime((String) null);
    }

    private T runtime(String message) {
        return runtime(e -> {
            if (message == null && e instanceof RuntimeException) return (RuntimeException) e;
            else return new RuntimeException(message != null ? message : e.getMessage(), e);
        });
    }

    private T runtime(Function<? super Exception, ? extends RuntimeException> wrapper) {
        if (isSuccess()) return value;
        else throw wrapper.apply(error);
    }

    private boolean isError() {
        return error != null;
    }

    private boolean isSuccess() {
        return !isError();
    }

    public T get() throws IllegalStateException {
        if (isSuccess()) {
            return value;
        } else {
            throw new IllegalStateException("Cannot get value if Try is in error state");
        }
    }

    public Exception getError() throws IllegalStateException {
        if (isError()) {
            return error;
        } else {
            throw new IllegalStateException("Cannot get error if Try is not in error state");
        }
    }

    public Try<T> ifFailed(Consumer<Exception> errorHandler) {
        if (isError()) {
            errorHandler.accept(error);
        }
        return this;
    }

    public <Handled extends Exception> Try<T> tryToRecover(Class<Handled> handledClass, ThrowingFunction<? super Exception, T, ? extends Exception> recoverFunction) {
        return tryToRecover(error -> {
            if (handledClass.isInstance(error)) return recoverFunction.apply(error);
            else throw error;
        });
    }

    public Try<T> tryToRecover(ThrowingFunction<? super Exception, T, ? extends Exception> recoverFunction) {
        if (isSuccess()) return new Try<>(value, null);
        else return exec(() -> recoverFunction.apply(error));
    }

    public Try<T> ifSuccess(Consumer<T> valueHandler) {
        if (isSuccess()) {
            valueHandler.accept(value);
        }
        return this;
    }
}
