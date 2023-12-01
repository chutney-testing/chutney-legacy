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

package com.chutneytesting.action.http.domain;


import static com.chutneytesting.action.common.SecurityUtils.buildSslContext;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class HttpClientFactory {

    private static final String PROXY_PROPERTY = "proxy";

    /**
     * @return an {@link HttpClient} depending on given {@link Target} able to handle:
     * <ul>
     * <li>no security</li>
     * <li>basic-auth</li>
     * <li>TLS (trust everything by default, hostname verifier is disabled)
     * <ul>
     * <li>TLS one-way</li>
     * <li>TLS two-way</li>
     * </ul>
     * </li>
     * </ul>
     */
    public HttpClient create(Logger logger, Target target, Class<String> responseType, int timeout) {
        RestTemplate restTemplate = buildRestTemplate(logger, target, timeout);

        return (httpMethod, resource, input) -> restTemplate.exchange(target.uri().toString() + resource, httpMethod, input, responseType);
    }

    private static RestTemplate buildRestTemplate(Logger logger, Target target, int timeout) {

        SSLContext sslContext;
        try {
            sslContext = buildSslContext(target).build();
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        final HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(socketFactory)
            .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeout, TimeUnit.MILLISECONDS).build())
            .build();
        final HttpClientBuilder httpClient = HttpClients.custom().setConnectionManager(connectionManager);

        final Optional<HttpRoutePlanner> httpRoutePlanner = getProxyConfiguration(logger, target);
        httpRoutePlanner.ifPresent(httpClient::setRoutePlanner);

        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient.build());
        requestFactory.setConnectTimeout(timeout);

        final RestTemplate restTemplate = new RestTemplate(requestFactory);
        configureBasicAuth(target, restTemplate);
        removeErrorHandler(restTemplate);
        return restTemplate;
    }

    private static Optional<HttpRoutePlanner> getProxyConfiguration(Logger logger, Target target) {
        if (isTargetProxySet(target)) {
            try {
                final String proxy = target.property(PROXY_PROPERTY).orElseThrow();
                final URL url = new URL(proxy);
                final String host = url.getHost();
                final String scheme = url.getProtocol();
                final int port = ofNullable(url.getPort()).orElse(3128);
                final HttpHost httpProxy = new HttpHost(scheme, host, port);
                logger.info("Proxy used: [" + httpProxy + "]");
                return of(new DefaultProxyRoutePlanner(httpProxy));
            } catch (MalformedURLException e) {
                logger.error("Malformed proxy url [" + target.property(PROXY_PROPERTY).get() + "]" + e.getMessage());
                return empty();
            }
        } else if (isSystemProxySet()) {
            return of(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
        }
        return empty();
    }

    private static boolean isTargetProxySet(Target target) {
        return target.property(PROXY_PROPERTY).isPresent();
    }

    private static void removeErrorHandler(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
    }

    private static void configureBasicAuth(Target target, RestTemplate restTemplate) {
        if (target.user().isPresent()) {
            String user = target.user().get();
            String password = target.userPassword().orElse("");
            restTemplate.getInterceptors().add(
                new BasicAuthenticationInterceptor(user, password, StandardCharsets.UTF_8)
            );
        }
    }

    private static Boolean isSystemProxySet() {
        return Stream.of("http.proxyHost", "https.proxyHost")
            .map(System::getProperty)
            .anyMatch(Objects::nonNull);
    }

    private static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) {
        }
    }
}
