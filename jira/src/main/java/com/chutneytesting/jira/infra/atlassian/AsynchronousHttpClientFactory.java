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

import static java.util.Date.from;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.annotation.Nonnull;

public final class AsynchronousHttpClientFactory {

    public static DisposableHttpClient createClient(
        final URI serverUri,
        final AuthenticationHandler authenticationHandler,
        final HttpClientOptions options
    ) {
        final DefaultHttpClientFactory<Object> defaultHttpClientFactory = new DefaultHttpClientFactory<>(
            new NoOpEventPublisher(),
            new RestClientApplicationProperties(serverUri),
            new NoOpThreadLocalContextManager<>()
        );

        final HttpClient httpClient = defaultHttpClientFactory.create(options);

        return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Override
            public void destroy() throws Exception {
                defaultHttpClientFactory.dispose(httpClient);
            }
        };
    }

    static final class NoOpThreadLocalContextManager<C> implements ThreadLocalContextManager<C> {
        @Override
        public C getThreadLocalContext() {
            return null;
        }

        @Override
        public void setThreadLocalContext(C context) {
        }

        @Override
        public void clearThreadLocalContext() {
        }
    }

    private static class NoOpEventPublisher implements EventPublisher {
        @Override
        public void publish(@Nonnull Object o) {
        }

        @Override
        public void register(@Nonnull Object o) {
        }

        @Override
        public void unregister(@Nonnull Object o) {
        }

        @Override
        public void unregisterAll() {
        }
    }

    private static class RestClientApplicationProperties implements ApplicationProperties {

        private final String baseUrl;

        private RestClientApplicationProperties(URI jiraURI) {
            this.baseUrl = jiraURI.getPath();
        }

        @Override
        public String getBaseUrl() {
            return baseUrl;
        }

        @Nonnull
        @Override
        public String getBaseUrl(UrlMode urlMode) {
            return baseUrl;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Chutney JIRA Rest Java Client";
        }

        @Nonnull
        @Override
        public String getPlatformId() {
            return ApplicationProperties.PLATFORM_JIRA;
        }

        @Nonnull
        @Override
        public String getVersion() {
            return MavenUtils.getVersion("com.atlassian.jira", "jira-rest-java-client-core");
        }

        @Nonnull
        @Override
        public Date getBuildDate() {
            return from(Instant.MIN);
        }

        @Nonnull
        @Override
        public String getBuildNumber() {
            return "0";
        }

        @Override
        public File getHomeDirectory() {
            return new File(".");
        }

        @Override
        public String getPropertyValue(final String s) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Nonnull
        @Override
        public String getApplicationFileEncoding() {
            return StandardCharsets.UTF_8.name();
        }

        @Nonnull
        @Override
        public Optional<Path> getLocalHomeDirectory() {
            return Optional.of(getHomeDirectory().toPath());
        }

        @Nonnull
        @Override
        public Optional<Path> getSharedHomeDirectory() {
            return getLocalHomeDirectory();
        }
    }
}
