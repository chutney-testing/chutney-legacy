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

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.DisposableBean;

final class DefaultHttpClientFactory<C> implements DisposableBean {
    private final EventPublisher eventPublisher;
    private final ApplicationProperties applicationProperties;
    private final ThreadLocalContextManager<C> threadLocalContextManager;
    private final Set<ApacheAsyncHttpClient<?>> httpClients = new CopyOnWriteArraySet<>();

    public DefaultHttpClientFactory(
        @Nonnull EventPublisher eventPublisher,
        @Nonnull ApplicationProperties applicationProperties,
        @Nonnull ThreadLocalContextManager<C> threadLocalContextManager) {
        this.eventPublisher = checkNotNull(eventPublisher);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
    }

    @Nonnull
    public HttpClient create(@Nonnull HttpClientOptions options) {
        return doCreate(options, threadLocalContextManager);
    }

    public void dispose(@Nonnull final HttpClient httpClient) throws Exception {
        if (httpClient instanceof ApacheAsyncHttpClient) {
            final ApacheAsyncHttpClient<?> client = (ApacheAsyncHttpClient<?>) httpClient;
            if (httpClients.remove(client)) {
                client.destroy();
            } else {
                throw new IllegalStateException("Client is already disposed");
            }
        } else {
            throw new IllegalArgumentException("Given client is not disposable");
        }
    }

    private <C> HttpClient doCreate(@Nonnull HttpClientOptions options, ThreadLocalContextManager<C> threadLocalContextManager) {
        checkNotNull(options);
        final ApacheAsyncHttpClient<C> httpClient = new ApacheAsyncHttpClient<>(eventPublisher, applicationProperties, threadLocalContextManager, options);
        httpClients.add(httpClient);
        return httpClient;
    }

    @Override
    public void destroy() throws Exception {
        for (ApacheAsyncHttpClient<?> httpClient : httpClients) {
            httpClient.destroy();
        }
    }
}
