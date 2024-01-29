/*
 * Copyright (C) 2012 Atlassian
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
package com.chutneytesting.jira.infra.atlassian;

import static com.google.common.base.Preconditions.checkNotNull;

import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import com.google.common.annotations.VisibleForTesting;
import io.atlassian.util.concurrent.Promise;
import io.atlassian.util.concurrent.Promises;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;

final class SettableFuturePromiseHttpPromiseAsyncClient<C> implements PromiseHttpAsyncClient {
    private final HttpAsyncClient client;
    private final ThreadLocalContextManager<C> threadLocalContextManager;
    private final Executor executor;

    SettableFuturePromiseHttpPromiseAsyncClient(HttpAsyncClient client, ThreadLocalContextManager<C> threadLocalContextManager, Executor executor) {
        this.client = checkNotNull(client);
        this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
        this.executor = new SettableFuturePromiseHttpPromiseAsyncClient.ThreadLocalDelegateExecutor<>(threadLocalContextManager, executor);
    }

    @Override
    public Promise<HttpResponse> execute(HttpUriRequest request, HttpContext context) {
        final CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        client.execute(request, context, new SettableFuturePromiseHttpPromiseAsyncClient.ThreadLocalContextAwareFutureCallback<C, HttpResponse>(threadLocalContextManager) {
            @Override
            void doCompleted(final HttpResponse httpResponse) {
                executor.execute(() -> future.complete(httpResponse));
            }

            @Override
            void doFailed(final Exception ex) {
                executor.execute(() -> future.completeExceptionally(ex));
            }

            @Override
            void doCancelled() {
                final TimeoutException timeoutException = new TimeoutException();
                executor.execute(() -> future.completeExceptionally(timeoutException));
            }
        });
        return Promises.forCompletionStage(future);
    }

    @VisibleForTesting
    static <C> void runInContext(ThreadLocalContextManager<C> threadLocalContextManager, C threadLocalContext, ClassLoader contextClassLoader, Runnable runnable) {
        final C oldThreadLocalContext = threadLocalContextManager.getThreadLocalContext();
        final ClassLoader oldCcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            threadLocalContextManager.setThreadLocalContext(threadLocalContext);
            runnable.run();
        } finally {
            threadLocalContextManager.setThreadLocalContext(oldThreadLocalContext);
            Thread.currentThread().setContextClassLoader(oldCcl);
        }
    }

    private static abstract class ThreadLocalContextAwareFutureCallback<C, HttpResponse> implements FutureCallback<HttpResponse> {
        private final ThreadLocalContextManager<C> threadLocalContextManager;
        private final C threadLocalContext;
        private final ClassLoader contextClassLoader;

        private ThreadLocalContextAwareFutureCallback(ThreadLocalContextManager<C> threadLocalContextManager) {
            this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
            this.threadLocalContext = threadLocalContextManager.getThreadLocalContext();
            this.contextClassLoader = Thread.currentThread().getContextClassLoader();
        }

        abstract void doCompleted(HttpResponse response);

        abstract void doFailed(Exception ex);

        abstract void doCancelled();

        @Override
        public final void completed(final HttpResponse response) {
            runInContext(threadLocalContextManager, threadLocalContext, contextClassLoader, () -> doCompleted(response));
        }

        @Override
        public final void failed(final Exception ex) {
            runInContext(threadLocalContextManager, threadLocalContext, contextClassLoader, () -> doFailed(ex));
        }

        @Override
        public final void cancelled() {
            runInContext(threadLocalContextManager, threadLocalContext, contextClassLoader, this::doCancelled);
        }
    }

    private static final class ThreadLocalDelegateExecutor<C> implements Executor {
        private final Executor delegate;
        private final ThreadLocalContextManager<C> manager;

        ThreadLocalDelegateExecutor(ThreadLocalContextManager<C> manager, Executor delegate) {
            this.delegate = checkNotNull(delegate);
            this.manager = checkNotNull(manager);
        }

        public void execute(@Nonnull final Runnable runnable) {
            delegate.execute(new SettableFuturePromiseHttpPromiseAsyncClient.ThreadLocalDelegateRunnable<>(manager, runnable));
        }
    }

    private static final class ThreadLocalDelegateRunnable<C> implements Runnable {
        private final C context;
        private final Runnable delegate;
        private final ClassLoader contextClassLoader;
        private final ThreadLocalContextManager<C> manager;

        ThreadLocalDelegateRunnable(ThreadLocalContextManager<C> manager, Runnable delegate) {
            this.delegate = delegate;
            this.manager = manager;
            this.context = manager.getThreadLocalContext();
            this.contextClassLoader = Thread.currentThread().getContextClassLoader();
        }

        public void run() {
            runInContext(manager, context, contextClassLoader, delegate);
        }
    }
}
