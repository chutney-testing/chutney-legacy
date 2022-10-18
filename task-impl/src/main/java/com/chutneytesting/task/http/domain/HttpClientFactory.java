package com.chutneytesting.task.http.domain;


import static com.chutneytesting.task.common.SecurityUtils.buildSslContext;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.chutneytesting.task.spi.injectable.Target;
import java.net.ProxySelector;
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

    private static final String PROXY_HOST_PROPERTY = "proxy.host";
    private static final String PROXY_PORT_PROPERTY = "proxy.port";
    private static final String PROXY_SCHEME_PROPERTY = "proxy.scheme";

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
    public HttpClient create(Target target, ParameterizedTypeReference<String> responseType, int timeout) {
        RestTemplate restTemplate = buildRestTemplate(target, timeout);

        return (httpMethod, resource, input) -> restTemplate.exchange(target.uri().toString() + resource, httpMethod, input, responseType);
    }

    public HttpClient create(Target target, Class<String> responseType, int timeout) {
        RestTemplate restTemplate = buildRestTemplate(target, timeout);

        return (httpMethod, resource, input) -> restTemplate.exchange(target.uri().toString() + resource, httpMethod, input, responseType);
    }

    private static RestTemplate buildRestTemplate(Target target, int timeout) {

        SSLContext sslContext;
        try {
            sslContext = buildSslContext(target).build();
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        HttpClientBuilder httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory);

        Optional<HttpRoutePlanner> httpRoutePlanner = getProxyConfiguration(target);
        if (httpRoutePlanner.isPresent()) {
            httpClient.setRoutePlanner(httpRoutePlanner.get());
        }

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient.build());
        requestFactory.setReadTimeout(timeout);
        requestFactory.setConnectTimeout(timeout);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        configureBasicAuth(target, restTemplate);
        removeErrorHandler(restTemplate);
        return restTemplate;
    }

    private static Optional<HttpRoutePlanner> getProxyConfiguration(Target target) {
        if (isTargetProxySet(target)) {
            String proxyHost = target.property(PROXY_HOST_PROPERTY).orElseThrow();
            Integer proxyPort = target.numericProperty(PROXY_PORT_PROPERTY).orElse(443).intValue();
            String proxyScheme = target.property(PROXY_SCHEME_PROPERTY).orElse("https");
            HttpHost proxy = new HttpHost(proxyHost, proxyPort, proxyScheme);
            return of(new DefaultProxyRoutePlanner(proxy));
        } else if (isSystemProxySet()) {
            return of(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
        }
        return empty();
    }

    private static boolean isTargetProxySet(Target target) {
        return target.property("proxy.host").isPresent();
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
