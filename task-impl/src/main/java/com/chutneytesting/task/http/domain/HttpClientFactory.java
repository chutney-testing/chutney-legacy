package com.chutneytesting.task.http.domain;


import static com.chutneytesting.task.common.SecurityUtils.buildSslContext;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.springframework.core.ParameterizedTypeReference;
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

        final HttpClientBuilder httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory);

        final Optional<HttpRoutePlanner> httpRoutePlanner = getProxyConfiguration(logger, target);
        httpRoutePlanner.ifPresent(httpClient::setRoutePlanner);

        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient.build());
        requestFactory.setReadTimeout(timeout);
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
                final int port = ofNullable(url.getPort()).orElse(defaultPort(scheme));
                final HttpHost httpProxy = new HttpHost(host, port, scheme);
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

    private static int defaultPort(String scheme) {
        switch (scheme) {
            case "https":
                return 443;
            case "http":
                return 80;
            default:
                return -1;
        }
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
