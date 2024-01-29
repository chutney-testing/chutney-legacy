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

import static io.atlassian.util.concurrent.Promises.rejected;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.TLS;
import static org.apache.http.nio.conn.ssl.SSLIOSessionStrategy.getDefaultHostnameVerifier;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.BannedHostResolver;
import com.atlassian.httpclient.apache.httpcomponents.BoundedHttpAsyncClient;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHostResolver;
import com.atlassian.httpclient.apache.httpcomponents.DefaultResponse;
import com.atlassian.httpclient.apache.httpcomponents.EntityTooLargeException;
import com.atlassian.httpclient.apache.httpcomponents.RedirectStrategy;
import com.atlassian.httpclient.apache.httpcomponents.RequestEntityEffect;
import com.atlassian.httpclient.apache.httpcomponents.cache.FlushableHttpCacheStorage;
import com.atlassian.httpclient.apache.httpcomponents.cache.FlushableHttpCacheStorageImpl;
import com.atlassian.httpclient.apache.httpcomponents.cache.LoggingHttpCacheStorage;
import com.atlassian.httpclient.api.HostResolver;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.HttpStatus;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponsePromises;
import com.atlassian.httpclient.api.ResponseTooLargeException;
import com.atlassian.httpclient.base.AbstractHttpClient;
import com.atlassian.httpclient.base.event.HttpRequestCompletedEvent;
import com.atlassian.httpclient.base.event.HttpRequestFailedEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import com.google.common.base.Throwables;
import com.google.common.primitives.Ints;
import io.atlassian.fugue.Suppliers;
import io.atlassian.util.concurrent.ThreadFactories;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpAsyncClient;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.ManagedNHttpClientConnectionFactory;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

final class ApacheAsyncHttpClient<C> extends AbstractHttpClient implements HttpClient, DisposableBean {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final Supplier<String> httpClientVersion = Suppliers.memoize(
        () -> MavenUtils.getVersion("com.atlassian.httpclient", "atlassian-httpclient-api"));

    private final Function<Object, Void> eventConsumer;
    private final Supplier<String> applicationName;
    private final ThreadLocalContextManager<C> threadLocalContextManager;
    private final ExecutorService callbackExecutor;
    private final HttpClientOptions httpClientOptions;

    private final CachingHttpAsyncClient httpClient;
    private final CloseableHttpAsyncClient nonCachingHttpClient;
    private final FlushableHttpCacheStorage httpCacheStorage;

    public ApacheAsyncHttpClient(EventPublisher eventConsumer, ApplicationProperties applicationProperties,
                                 ThreadLocalContextManager<C> threadLocalContextManager) {
        this(eventConsumer, applicationProperties, threadLocalContextManager, new HttpClientOptions());
    }

    public ApacheAsyncHttpClient(EventPublisher eventConsumer,
                                 ApplicationProperties applicationProperties,
                                 ThreadLocalContextManager<C> threadLocalContextManager,
                                 HttpClientOptions options) {
        this(new DefaultApplicationNameSupplier(applicationProperties),
            new EventConsumerFunction(eventConsumer),
            threadLocalContextManager,
            options);
    }

    public ApacheAsyncHttpClient(final Supplier<String> applicationName,
                                 final Function<Object, Void> eventConsumer,
                                 final ThreadLocalContextManager<C> threadLocalContextManager,
                                 final HttpClientOptions options) {
        this.eventConsumer = requireNonNull(eventConsumer, "eventConsumer can't be null");
        this.applicationName = requireNonNull(applicationName, "applicationName can't be null");
        this.threadLocalContextManager = requireNonNull(threadLocalContextManager, "threadLocalContextManager can't be null");
        this.httpClientOptions = requireNonNull(options, "options can't be null");

        try {
            final IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(options.getIoThreadCount())
                .setSelectInterval(options.getIoSelectInterval())
                .setInterestOpQueued(true)
                .build();

            final DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(reactorConfig);
            ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {
                @Override
                public boolean handle(final IOException e) {
                    log.error("IO exception in reactor ", e);
                    return false;
                }

                @Override
                public boolean handle(final RuntimeException e) {
                    log.error("Fatal runtime error", e);
                    return false;
                }
            });

            List<String> bannedAddresses = options.getBlacklistedAddresses();
            HostResolver resolver;
            if (bannedAddresses.isEmpty()) {
                resolver = DefaultHostResolver.INSTANCE;
            } else {
                resolver = new BannedHostResolver(bannedAddresses);
            }

            final PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(
                ioReactor,
                ManagedNHttpClientConnectionFactory.INSTANCE,
                getRegistry(options),
                DefaultSchemePortResolver.INSTANCE,
                resolver::resolve,
                options.getConnectionPoolTimeToLive(),
                TimeUnit.MILLISECONDS) {
                @SuppressWarnings("MethodDoesntCallSuperMethod")
                @Override
                protected void finalize() {
                    // prevent the PoolingClientAsyncConnectionManager from logging - this causes exceptions due to
                    // the ClassLoader probably having been removed when the plugin shuts down.  Added a
                    // PluginEventListener to make sure the shutdown method is called while the plugin classloader
                    // is still active.
                }
            };

            final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout((int) options.getConnectionTimeout())
                .setConnectionRequestTimeout((int) options.getLeaseTimeout())
                .setCookieSpec(options.getIgnoreCookies() ? CookieSpecs.IGNORE_COOKIES : CookieSpecs.DEFAULT)
                .setSocketTimeout((int) options.getSocketTimeout())
                .build();

            connectionManager.setDefaultMaxPerRoute(options.getMaxConnectionsPerHost());
            connectionManager.setMaxTotal(options.getMaxTotalConnections());

            final HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom()
                .setThreadFactory(ThreadFactories.namedThreadFactory(options.getThreadPrefix() + "-io", ThreadFactories.Type.DAEMON))
                .setDefaultIOReactorConfig(reactorConfig)
                .setConnectionManager(connectionManager)
                .setRedirectStrategy(new RedirectStrategy())
                .setUserAgent(getUserAgent(options))
                .setDefaultRequestConfig(requestConfig);

            // set up a route planner if there is proxy configuration
            options.getProxyOptions().getProxyHosts().values().stream().findFirst().ifPresent(proxyHost -> {
                clientBuilder.setRoutePlanner(
                    (target, request, context) ->
                        new HttpRoute(target, new HttpHost(proxyHost.getHostName(), proxyHost.getPort(), proxyHost.getSchemeName()))
                );
            });
//            ProxyConfigFactory.getProxyConfig(options).forEach(proxyConfig -> {
//                // don't be fooled by its name. If SystemDefaultRoutePlanner is passed a proxy selector it will use that
//                // instead of creating the default one that reads system properties
//                clientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(DefaultSchemePortResolver.INSTANCE, proxyConfig.toProxySelector()));
//
//                ProxyCredentialsProvider.build(options).forEach(credsProvider -> {
//                    clientBuilder.setProxyAuthenticationStrategy(ProxyAuthenticationStrategy.INSTANCE);
//                    clientBuilder.setDefaultCredentialsProvider(credsProvider);
//                });
//            });

            this.nonCachingHttpClient = new BoundedHttpAsyncClient(clientBuilder.build(),
                Ints.saturatedCast(options.getMaxEntitySize()));

            final CacheConfig cacheConfig = CacheConfig.custom()
                .setMaxCacheEntries(options.getMaxCacheEntries())
                .setSharedCache(false)
                .setNeverCacheHTTP10ResponsesWithQueryString(false)
                .setMaxObjectSize(options.getMaxCacheObjectSize())
                .build();

            this.httpCacheStorage = new LoggingHttpCacheStorage(new FlushableHttpCacheStorageImpl(cacheConfig));
            this.httpClient = new CachingHttpAsyncClient(nonCachingHttpClient, httpCacheStorage, cacheConfig);
            this.callbackExecutor = options.getCallbackExecutor();

            nonCachingHttpClient.start();
        } catch (IOReactorException e) {
            throw new RuntimeException("Reactor " + options.getThreadPrefix() + "not set up correctly", e);
        }
    }

    private Registry<SchemeIOSessionStrategy> getRegistry(final HttpClientOptions options) {
        try {
            final SSLContext sslContext;
            if (options.trustSelfSignedCertificates()) {
                sslContext = SSLContexts.custom()
                    .setProtocol(TLS)
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .build();
            } else {
                sslContext = SSLContexts.createSystemDefault();
            }

            final SSLIOSessionStrategy sslioSessionStrategy = new SSLIOSessionStrategy(
                sslContext,
                firstNonNull(options.getSupportedProtocols(), split(System.getProperty("https.protocols"))),
                split(System.getProperty("https.cipherSuites")),
                options.trustSelfSignedCertificates() ? getSelfSignedVerifier() : getDefaultHostnameVerifier());

            return RegistryBuilder.<SchemeIOSessionStrategy>create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", sslioSessionStrategy)
                .build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            return getFallbackRegistry(e);
        }
    }

    private HostnameVerifier getSelfSignedVerifier() {
        return (host, session) -> {
            log.debug("Verification for certificates from {} disabled", host);
            return true;
        };
    }

    private Registry<SchemeIOSessionStrategy> getFallbackRegistry(final GeneralSecurityException e) {
        log.error("Error when creating scheme session strategy registry", e);
        return RegistryBuilder.<SchemeIOSessionStrategy>create()
            .register("http", NoopIOSessionStrategy.INSTANCE)
            .register("https", SSLIOSessionStrategy.getDefaultStrategy())
            .build();
    }

    private String getUserAgent(HttpClientOptions options) {
        return format("Atlassian HttpClient %s / %s / %s",
            httpClientVersion.get(),
            applicationName.get(),
            options.getUserAgent());
    }

    @Override
    public final ResponsePromise execute(final Request request) {
        try {
            return doExecute(request);
        } catch (Throwable t) {
            return ResponsePromises.toResponsePromise(rejected(t));
        }
    }

    private ResponsePromise doExecute(final Request request) {
        httpClientOptions.getRequestPreparer().accept(request);

        final long start = System.currentTimeMillis();
        final HttpRequestBase op;
        final String uri = request.getUri().toString();
        final Request.Method method = request.getMethod();
        switch (method) {
            case GET:
                op = new HttpGet(uri);
                break;
            case POST:
                op = new HttpPost(uri);
                break;
            case PUT:
                op = new HttpPut(uri);
                break;
            case DELETE:
                op = new HttpDelete(uri);
                break;
            case OPTIONS:
                op = new HttpOptions(uri);
                break;
            case HEAD:
                op = new HttpHead(uri);
                break;
            case TRACE:
                op = new HttpTrace(uri);
                break;
            default:
                throw new UnsupportedOperationException(method.toString());
        }
        if (request.hasEntity()) {
            new RequestEntityEffect(request).apply(op);
        }

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            op.setHeader(entry.getKey(), entry.getValue());
        }

        final PromiseHttpAsyncClient asyncClient = getPromiseHttpAsyncClient(request);
        return ResponsePromises.toResponsePromise(asyncClient.execute(op, new BasicHttpContext()).fold(
            ex -> {
                final long requestDuration = System.currentTimeMillis() - start;
                Throwable exception = maybeTranslate(ex);
                publishEvent(request, requestDuration, exception);
                Throwables.throwIfUnchecked(exception);
                throw new RuntimeException(exception);
            },
            httpResponse -> {
                final long requestDuration = System.currentTimeMillis() - start;
                publishEvent(request, requestDuration, httpResponse.getStatusLine().getStatusCode());
                try {
                    return translate(httpResponse);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        ));
    }

    private void publishEvent(Request request, long requestDuration, int statusCode) {
        if (HttpStatus.OK.code <= statusCode && statusCode < HttpStatus.MULTIPLE_CHOICES.code) {
            eventConsumer.apply(new HttpRequestCompletedEvent(
                request.getUri().toString(),
                request.getMethod().name(),
                statusCode,
                requestDuration,
                request.getAttributes()));
        } else {
            eventConsumer.apply(new HttpRequestFailedEvent(
                request.getUri().toString(),
                request.getMethod().name(),
                statusCode,
                requestDuration,
                request.getAttributes()));
        }
    }

    private void publishEvent(Request request, long requestDuration, Throwable ex) {
        eventConsumer.apply(new HttpRequestFailedEvent(
            request.getUri().toString(),
            request.getMethod().name(),
            ex.toString(),
            requestDuration,
            request.getAttributes()));
    }

    private PromiseHttpAsyncClient getPromiseHttpAsyncClient(Request request) {
        return new SettableFuturePromiseHttpPromiseAsyncClient<>(
            request.isCacheDisabled() ? nonCachingHttpClient : httpClient,
            threadLocalContextManager, callbackExecutor);
    }

    private Throwable maybeTranslate(Throwable ex) {
        if (ex instanceof EntityTooLargeException) {
            EntityTooLargeException tooLarge = (EntityTooLargeException) ex;
            try {
                // don't include the cause to ensure that the HttpResponse is released
                return new ResponseTooLargeException(translate(tooLarge.getResponse()), ex.getMessage());
            } catch (IOException e) {
                // could not translate, just return the original exception
            }
        }
        return ex;
    }

    private Response translate(HttpResponse httpResponse) throws IOException {
        StatusLine status = httpResponse.getStatusLine();
        Response.Builder responseBuilder = DefaultResponse.builder()
            .setMaxEntitySize(httpClientOptions.getMaxEntitySize())
            .setStatusCode(status.getStatusCode())
            .setStatusText(status.getReasonPhrase());

        Header[] httpHeaders = httpResponse.getAllHeaders();
        for (Header httpHeader : httpHeaders) {
            responseBuilder.setHeader(httpHeader.getName(), httpHeader.getValue());
        }
        final HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            responseBuilder.setEntityStream(entity.getContent());
        }
        return responseBuilder.build();
    }

    @Override
    public void destroy() throws Exception {
        callbackExecutor.shutdown();
        nonCachingHttpClient.close();
    }

    @Override
    public void flushCacheByUriPattern(Pattern urlPattern) {
        httpCacheStorage.flushByUriPattern(urlPattern);
    }

    private static final class DefaultApplicationNameSupplier implements Supplier<String> {
        private final ApplicationProperties applicationProperties;

        DefaultApplicationNameSupplier(ApplicationProperties applicationProperties) {
            this.applicationProperties = requireNonNull(applicationProperties);
        }

        @Override
        public String get() {
            return format("%s-%s (%s)",
                applicationProperties.getDisplayName(),
                applicationProperties.getVersion(),
                applicationProperties.getBuildNumber());
        }
    }

    private static class EventConsumerFunction implements Function<Object, Void> {
        private final EventPublisher eventPublisher;

        EventConsumerFunction(EventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        @Override
        public Void apply(Object event) {
            eventPublisher.publish(event);
            return null;
        }
    }

    private static String[] split(final String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }
}
